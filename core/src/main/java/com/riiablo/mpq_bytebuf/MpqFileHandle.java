package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ReferenceCountUpdater;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.artemis.utils.BitVector;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.DecodingService.ArchiveRead;
import com.riiablo.mpq_bytebuf.DecodingService.Callback;
import com.riiablo.mpq_bytebuf.DecodingService.DecodingTask;

import static com.riiablo.mpq_bytebuf.DecodingService.IGNORE;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_ENCRYPTED;
import static com.riiablo.mpq_bytebuf.Mpq.Block.FLAG_EXISTS;
import static com.riiablo.util.ImplUtils.unsupported;

public final class MpqFileHandle extends FileHandle implements ReferenceCounted {
  private static final Logger log = LogManager.getLogger(MpqFileHandle.class);

  private static final boolean DEBUG_MODE = !true;

  /*
   * Implementation note:
   *
   * File decoding is thread-safe, however reading from mpq archive must be
   * synchronized on Mpq#lock() due to an issue with concurrent copying from
   * the underlying nio byte buffer.
   *
   * File decoding is split into relevant sectors without regard to whether a
   * sector may already be decoding (or scheduled for decoding). This is a
   * future optimization, but also plausibly a corner-case. The resulting
   * behavior at this time is to schedule an additional decoding task for that
   * sector, and when that task starts, lock and check if that sector has since
   * been decoded, and thus skip decoding that sector.
   *
   * Alternatively, this could be implemented by managing futures for discrete
   * sector decode tasks and returning the existing future for a sector that
   * may be scheduled again.
   */

  private static final long REFCNT_FIELD_OFFSET =
      ReferenceCountUpdater.getUnsafeOffset(MpqFileHandle.class, "refCnt");
  private static final AtomicIntegerFieldUpdater<MpqFileHandle> AIF_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(MpqFileHandle.class, "refCnt");

  private static final ReferenceCountUpdater<MpqFileHandle> updater =
      new ReferenceCountUpdater<MpqFileHandle>() {
    @Override
    protected AtomicIntegerFieldUpdater<MpqFileHandle> updater() {
      return AIF_UPDATER;
    }

    @Override
    protected long unsafeOffset() {
      return REFCNT_FIELD_OFFSET;
    }
  };

  // Value might not equal "real" reference count, all access should be via the updater
  @SuppressWarnings("unused")
  private volatile int refCnt = updater.initialValue();

  final DecodingService decoder;
  public final Mpq mpq;
  final int index;
  public final String filename;
  String toString;

  // MPQ block data
  final int offset;
  final int CSize;
  final int FSize;
  final int flags;

  // Buffer
  ByteBuf archive; // direct slice of archive (compressed data)
  final BitVector decoded = new BitVector();
  int numSectors = -1; // number of sectors, single contiguous block of memory
  ByteBuf sectorOffsets; // direct slice of archive or decoded heap bytebuf
  ByteBuf buffer; // heap bytebuf of decoded data
  int encryptionKey;

  MpqFileHandle(
      DecodingService decoder,
      Mpq mpq,
      int index,
      String filename,
      int offset,
      int CSize,
      int FSize,
      int flags
  ) {
    this.decoder = decoder;
    this.mpq = mpq;
    this.index = index;
    this.filename = filename;

    this.offset = offset;
    this.CSize = CSize;
    this.FSize = FSize;
    this.flags = flags;

    this.archive = mpq.map().slice(offset, CSize);
  }

  @Override
  public String toString() {
    return toString == null
        ? toString = mpq + ":" + filename
        : toString;
  }

  @Override
  public String path() {
    return filename;
  }

  @Override
  public String name() {
    return FilenameUtils.getName(filename);
  }

  @Override
  public String extension() {
    return FilenameUtils.getExtension(filename);
  }

  @Override
  public String nameWithoutExtension() {
    return FilenameUtils.getBaseName(filename);
  }

  @Override
  public String pathWithoutExtension() {
    return unsupported("not supported for mpq files");
  }

  public int flags() {
    return flags;
  }

  public boolean uncompressed() {
    return (flags & ~FLAG_EXISTS) == 0;
  }

  public boolean encrypted() {
    return (flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED;
  }

  /**
   * Returns a buffer containing decompressed contents of this mpq file.
   * Returned buffer is managed by the handle itself and does not need to be
   * released, instead the handle must be released when it is no longer needed.
   *
   * @see #buffer(int, int)
   * @see #bufferAsync(Callback)
   * @see #bufferAsync(int, int, Callback)
   * @see #release()
   */
  public ByteBuf buffer() {
    return buffer(0, FSize);
  }

  /**
   * Returns a buffer containing decompressed contents of this mpq file.
   * Returned buffer is managed by the handle itself and does not need to be
   * released, instead the handle must be released when it is no longer needed.
   *
   * @param offset starting offset in decompressed contents
   * @param length length of decompressed contents after offset
   *
   * @see #buffer()
   * @see #bufferAsync(Callback)
   * @see #bufferAsync(int, int, Callback)
   * @see #release()
   */
  public ByteBuf buffer(int offset, int length) {
    if (offset + length > FSize) {
      throw new IndexOutOfBoundsException(
          String.format(
              "offset(+0x%x) + length(0x%x) exceeds declared FSize(0x%x)",
              offset, length, FSize));
    }

    Future<ByteBuf> future = ensureReadable(offset, length, IGNORE);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public Future<ByteBuf> bufferAsync() {
    return bufferAsync(IGNORE);
  }

  /**
   * Schedules the contents of this mpq file for decoding and returns a future
   * used to track the progress. Decoding the buffer may take a lot of time
   * decoding, therefore a callback can be passed and used to perform an action
   * on the decoded contents when they are available.
   * <p/>
   * Returned buffer is managed by the handle itself and does not need to be
   * released, instead the handle must be released when it is no longer needed.
   *
   * @see #buffer()
   * @see #buffer(int, int)
   * @see #bufferAsync()
   * @see #bufferAsync(int, int, Callback)
   * @see #release()
   */
  public Future<ByteBuf> bufferAsync(Callback callback) {
    return bufferAsync(0, FSize, callback);
  }

  public Future<ByteBuf> bufferAsync(int offset, int length, Callback callback) {
    if (offset + length > FSize) {
      throw new IndexOutOfBoundsException(
          String.format(
              "offset(+0x%x) + length(0x%x) exceeds declared FSize(0x%x)",
              offset, length, FSize));
    }

    return ensureReadable(offset, length, callback);
  }

  int encryptionKey() {
    return encryptionKey == 0
        ? encryptionKey = Mpq.encryptionKey(filename, flags, offset, FSize)
        : encryptionKey;
  }

  Future<ByteBuf> ensureReadable(final int offset, final int length, final Callback callback) {
    if (numSectors < 0) {
      readSectorOffsets();
      allocateBuffer();
    }

    return numSectors == 0
        ? readRawArchive(callback)
        : decodeSectors(offset, length, callback);
  }

  ByteBuf readSectorOffsets() {
    assert numSectors < 0 : "sector offsets already read!";
    if (uncompressed()) {
      numSectors = 0;
      sectorOffsets = Unpooled.EMPTY_BUFFER;
      return sectorOffsets;
    }

    final int sectorSize = mpq.sectorSize;
    final int numSectors = this.numSectors = ((FSize + sectorSize - 1) / sectorSize);
    final int sectorTableSize = (numSectors + 1) << 2; // accommodate offset of sector offset table
    decoded.ensureCapacity(numSectors);

    if (!encrypted()) {
      sectorOffsets = archive.slice(0, sectorTableSize);
      return sectorOffsets;
    }

    log.trace("Decrypting sector offsets table...");
    sectorOffsets = mpq.fileBuffer(sectorTableSize);
    sectorOffsets.writeBytes(archive, 0, sectorTableSize);
    Decrypter.decrypt(encryptionKey() - 1, sectorOffsets);
    return sectorOffsets;
  }

  ByteBuf allocateBuffer() {
    assert buffer == null : "buffer != null";
    buffer = mpq.fileBuffer(FSize);
    return buffer;
  }

  Future<ByteBuf> readRawArchive(Callback callback) {
    assert numSectors == 0 : "copyBuffer requires numSectors=" + numSectors;
    final boolean decoded;
    synchronized (this.decoded) {
      decoded = this.decoded.unsafeGet(0); // using bit 0 as decoded tag for buffer
    }

    if (decoded) {
      final ByteBuf buffer = this.buffer;
      callback.onDecoded(this, 0, FSize, buffer);
      return ConcurrentUtils.constantFuture(buffer);
    }

    return decoder.submit(new ArchiveRead(this, 0, FSize, buffer, 0, callback));
  }

  Future<ByteBuf> decodeSectors(int offset, int length, Callback callback) {
    final int sectorSize = mpq.sectorSize;
    int startSector = offset / sectorSize;
    int endSector = (offset + length + sectorSize - 1) / sectorSize;
    DecodingTask.Builder task = null;
    for (int i = startSector; i < endSector; i++) {
      synchronized (decoded) { if (decoded.unsafeGet(i)) continue; }
      final int bufferOffset = i * sectorSize;
      final int sectorOffset = sectorOffsets.getIntLE(i << 2);
      final int nextSectorOffset = sectorOffsets.getIntLE((i + 1) << 2);
      final int sectorCSize = nextSectorOffset - sectorOffset;
      final int sectorFSize = Math.min(FSize - bufferOffset, sectorSize);
      if (task == null) {
        task = DecodingTask.builder(
            this,
            offset,
            length,
            endSector - i,
            callback);
      }
      task.add(i, sectorOffset, sectorCSize, sectorFSize, buffer, bufferOffset);
    }

    if (task != null) {
      if (DEBUG_MODE) log.trace("Submitting {} sectors for decoding", task.size());
      return decoder.submit(task.build());
    }

    ByteBuf buffer = this.buffer.slice(offset, length);
    callback.onDecoded(this, offset, length, buffer);
    return ConcurrentUtils.constantFuture(buffer);
  }

  public InputStream stream() {
    return stream(0, FSize, true);
  }

  public InputStream stream(boolean releaseOnClose) {
    return stream(0, FSize, releaseOnClose);
  }

  public InputStream stream(int offset, int length, boolean releaseOnClose) {
    return MpqStream.open(this, offset, length, releaseOnClose);
  }

  @Override
  public int refCnt() {
    return updater.refCnt(this);
  }

  @Override
  public MpqFileHandle retain() {
    return updater.retain(this);
  }

  @Override
  public MpqFileHandle retain(int increment) {
    return updater.retain(this, increment);
  }

  @Override
  public MpqFileHandle touch() {
    return touch(null);
  }

  @Override
  public MpqFileHandle touch(Object hint) {
    return this;
  }

  @Override
  public boolean release() {
    return handleRelease(updater.release(this));
  }

  @Override
  public boolean release(int decrement) {
    return handleRelease(updater.release(this, decrement));
  }

  private boolean handleRelease(boolean release) {
    if (release) deallocate();
    return release;
  }

  /**
   * Called once {@link #refCnt()} is equals 0. Deallocates decoded sectors and
   * sector offsets which are shared amongst references of this file handle.
   */
  void deallocate() {
    mpq.dispose(index);
    if (numSectors < 0) return;
    numSectors = -1;
    decoded.clear();
    releaseSectorOffsets();
    releaseBuffer();
  }

  void releaseSectorOffsets() {
    ReferenceCountUtil.safeRelease(sectorOffsets);
    sectorOffsets = null;
  }

  void releaseBuffer() {
    ReferenceCountUtil.safeRelease(buffer);
    buffer = null;
  }

  void dispose() {
    ReferenceCountUtil.safeRelease(archive);
    archive = null;
  }

  @Override
  public Files.FileType type() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public File file() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public InputStream read() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public BufferedInputStream read(int bufferSize) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public Reader reader() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public Reader reader(String charset) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public BufferedReader reader(int bufferSize) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public BufferedReader reader(int bufferSize, String charset) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public String readString() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public String readString(String charset) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public byte[] readBytes() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public int readBytes(byte[] bytes, int offset, int size) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public ByteBuffer map() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public ByteBuffer map(FileChannel.MapMode mode) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public OutputStream write(boolean append) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public OutputStream write(boolean append, int bufferSize) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public void write(InputStream input, boolean append) {
    unsupported("not supported for mpq files");
  }

  @Override
  public Writer writer(boolean append) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public Writer writer(boolean append, String charset) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public void writeString(String string, boolean append) {
    unsupported("not supported for mpq files");
  }

  @Override
  public void writeString(String string, boolean append, String charset) {
    unsupported("not supported for mpq files");
  }

  @Override
  public void writeBytes(byte[] bytes, boolean append) {
    unsupported("not supported for mpq files");
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
    unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle[] list() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle[] list(String suffix) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle[] list(FileFilter filter) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle[] list(FilenameFilter filter) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public FileHandle child(String name) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle sibling(String name) {
    return unsupported("not supported for mpq files");
  }

  @Override
  public FileHandle parent() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public void mkdirs() {
    unsupported("not supported for mpq files");
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public boolean delete() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public boolean deleteDirectory() {
    return unsupported("not supported for mpq files");
  }

  @Override
  public void emptyDirectory() {
    unsupported("not supported for mpq files");
  }

  @Override
  public void emptyDirectory(boolean preserveTree) {
    unsupported("not supported for mpq files");
  }

  @Override
  public void copyTo(FileHandle dest) {
    unsupported("not supported for mpq files");
  }

  @Override
  public void moveTo(FileHandle dest) {
    unsupported("not supported for mpq files");
  }

  @Override
  public long length() {
    return FSize;
  }

  @Override
  public long lastModified() {
    return unsupported("not supported for mpq files");
  }
}
