package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
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
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.artemis.utils.BitVector;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.DecoderExecutorGroup.DecodingTask;

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

  final DecoderExecutorGroup decoder;
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
      DecoderExecutorGroup decoder,
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

  /**
   * Matches FileHandle irrespective of {@link FileHandle#type}
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof MpqFileHandle) {
      MpqFileHandle other = (MpqFileHandle) o;
      return Objects.equals(filename, other.path());
    } else {
      if (!(o instanceof FileHandle)) return false;
      FileHandle other = (FileHandle) o;
      return super.equals(other);
    }
  }

  @Override
  public int hashCode() {
    return filename.hashCode();
  }

  @Override
  public String toString() {
    return toString == null
        ? toString = mpq + ":" + filename
        : toString;
  }

  public int sectorSize() {
    return mpq.sectorSize;
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
   * @see #bufferAsync(EventExecutor)
   * @see #bufferAsync(EventExecutor, int, int)
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
   * @see #bufferAsync(EventExecutor)
   * @see #bufferAsync(EventExecutor, int, int)
   * @see #release()
   */
  public ByteBuf buffer(int offset, int length) {
    try {
      return bufferAsync(ImmediateEventExecutor.INSTANCE, offset, length).get();
    } catch (InterruptedException | CancellationException | ExecutionException t) {
      return ExceptionUtils.rethrow(t);
    }
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
   * @see #bufferAsync(EventExecutor)
   * @see #bufferAsync(EventExecutor, int, int)
   * @see #release()
   */
  public Future<ByteBuf> bufferAsync(EventExecutor executor) {
    return bufferAsync(executor, 0, FSize);
  }

  public Future<ByteBuf> bufferAsync(EventExecutor executor, int offset, int length) {
    if (offset + length > FSize) {
      throw new IndexOutOfBoundsException(
          String.format(
              "offset(+0x%x) + length(0x%x) exceeds declared FSize(0x%x)",
              offset, length, FSize));
    }

    return ensureReadable(executor, offset, length);
  }

  int encryptionKey() {
    return encryptionKey == 0
        ? encryptionKey = Mpq.encryptionKey(filename, flags, offset, FSize)
        : encryptionKey;
  }

  Future<ByteBuf> ensureReadable(EventExecutor executor, int offset, int length) {
    if (numSectors < 0) {
      readSectorOffsets();
      allocateBuffer();
    }

    return numSectors == 0
        ? readRawArchive(executor, offset, length)
        : decodeSectors(executor, offset, length);
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

  Future<ByteBuf> readRawArchive(EventExecutor executor, final int offset, final int length) {
    assert numSectors == 0 : "copyBuffer requires numSectors=" + numSectors;
    if (decoded(0)) { // using bit 0 as decoded tag for buffer
      final ByteBuf buffer = this.buffer.slice(offset, length).writerIndex(length);
      return executor.newSucceededFuture(buffer);
    }

    final Promise<ByteBuf> promise = executor.newPromise();
    decoder
        .newArchiveReadTask(executor, this, 0, FSize, buffer, 0)
        .submit()
        .addListener(new FutureListener<ByteBuf>() {
          @Override
          public void operationComplete(Future<ByteBuf> future) {
            setDecoded(0, buffer); // using bit 0 as decoded tag for buffer
            promise.setSuccess(buffer.slice(offset, length).writerIndex(length));
          }
        });
    return promise;
  }

  Future<ByteBuf> decodeSectors(EventExecutor executor, final int offset, final int length) {
    final int sectorSize = mpq.sectorSize;
    int startSector = offset / sectorSize;
    int endSector = (offset + length + sectorSize - 1) / sectorSize;
    DecodingTask task = null;
    for (int i = startSector; i < endSector; i++) {
      final int sector = i;
      if (decoded(sector)) continue;
      final int bufferOffset = sector * sectorSize;
      final int sectorOffset = sectorOffsets.getIntLE(sector << 2);
      final int nextSectorOffset = sectorOffsets.getIntLE((sector + 1) << 2);
      final int sectorCSize = nextSectorOffset - sectorOffset;
      final int sectorFSize = Math.min(FSize - bufferOffset, sectorSize);
      if (task == null) task = decoder.newDecodingTask(executor, this, offset, length);
      // if (buffer == null) throw new AssertionError("buffer was null?");
      final ByteBuf buffer = this.buffer;
      task.submit(sector, sectorOffset, sectorCSize, sectorFSize, buffer, bufferOffset)
          .addListener(new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) {
              setDecoded(sector, buffer);
            }
          });
    }

    if (task != null) {
      if (DEBUG_MODE) log.trace("Submitting {} sectors for decoding", task.numTasks());
      final Promise<ByteBuf> aggregatePromise = executor.newPromise();
      task.combine(executor.<Void>newPromise())
          .addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) {
              aggregatePromise.setSuccess(buffer.slice(offset, length).writerIndex(length));
            }
          });
      return aggregatePromise;
    }

    ByteBuf buffer = this.buffer.slice(offset, length).writerIndex(length);
    return executor.newSucceededFuture(buffer);
  }

  /**
   * Queries whether the buffer backed by this file handle already contains the
   * decoded contents of the specified sector.
   *
   * @see #decoded(int, ByteBuf)
   */
  boolean decoded(final int sector) {
    return decoded(sector, this.buffer);
  }

  /**
   * Queries whether the buffer backed by this file handle already contains the
   * decoded contents of the specified sector, and that the given buffer is the
   * backing buffer (and thus contains those contents).
   *
   * @see #decoded(int)
   */
  boolean decoded(final int sector, final ByteBuf buffer) {
    assert buffer != null : "buffer cannot be null";
    if (this.buffer != buffer) return false;
    synchronized (decoded) { return decoded.get(sector); }
  }

  /**
   * Marks the specified sector as decoded iff {@code buffer} is the backing
   * buffer.
   */
  void setDecoded(final int sector, final ByteBuf buffer) {
    assert buffer != null : "buffer cannot be null";
    if (this.buffer != buffer) return;
    synchronized (decoded) { decoded.unsafeSet(sector); }
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

  public Future<InputStream> bufferStream(EventExecutor executor, int bufferSize) {
    return new MpqBufferStream(this, executor, bufferSize, false).initialize();
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
    // required by music files, restricting for everything else
    return FilenameUtils.isExtension(filename, "WAV")
        ? stream(true)
        : unsupported("not supported for mpq files");
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

  public FutureListener<ByteBuf> releasingFuture() {
    return new ReleasingFuture(this);
  }

  public static final class ReleasingFuture implements FutureListener<ByteBuf> {
    final MpqFileHandle handle;

    public ReleasingFuture(MpqFileHandle handle) {
      this.handle = handle;
    }

    @Override
    public void operationComplete(Future<ByteBuf> future) {
      log.info("Releasing {}", handle);
      handle.release();
    }
  }
}
