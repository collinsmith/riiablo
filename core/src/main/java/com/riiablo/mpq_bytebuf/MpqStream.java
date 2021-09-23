package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.io.InputStream;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.mpq_bytebuf.Decrypter.ENCRYPTION;
import static com.riiablo.mpq_bytebuf.Decrypter.SEED2;

/**
 * InputStream of a mpq file. Optimized s.t. an uncompressed stream can be
 * used directly from the slice of the mpq archive, or decoded using structures
 * maintained by this stream.
 * <p>
 * TODO: improve fix placeholder doc
 */
public final class MpqStream extends InputStream {
  private static final Logger log = LogManager.getLogger(MpqStream.class);

  private static final boolean DEBUG_MODE = !true;

  final MpqFileHandle handle;
  final boolean releaseOnClose;
  final boolean encrypted;
  ByteBuf buffer;

  int key;
  int seed;
  int sectorOffset;
  int nextSectorOffset;
  ByteBuf sectorOffsets;

  final int numSectors;
  final int startSector;
  final int endSector;
  int currentSector;

  int bytesRead;
  int limit;

  static InputStream open(
      MpqFileHandle handle,
      int offset,
      int length,
      boolean releaseOnClose
  ) {
    if (handle.uncompressed()) {
      final ByteBuf buffer = handle.archive.slice(offset, length);
      return new ByteBufInputStream(buffer, length, releaseOnClose);
    }

    return new MpqStream(handle, offset, length, releaseOnClose);
  }

  MpqStream(MpqFileHandle handle) {
    this(handle, 0, handle.FSize, true);
  }

  MpqStream(MpqFileHandle handle, int offset, int length, boolean releaseOnClose) {
    super();
    if (handle.uncompressed()) {
      throw new IllegalStateException(""
          + "cannot construct stream from uncompressed file handle, "
          + "use MpqStream#open() instead");
    }

    this.handle = handle;
    this.encrypted = handle.encrypted();
    this.releaseOnClose = releaseOnClose;
    this.bytesRead = offset;
    this.limit = offset + length;
    this.buffer = handle.mpq.sectorBuffer();

    if (encrypted) {
      key = handle.encryptionKey() - 1;
      seed = SEED2;
    }

    final int sectorSize = handle.mpq.sectorSize;
    final int numSectors = this.numSectors = ((handle.FSize + sectorSize - 1) / sectorSize);
    final int sectorTableSize = (numSectors + 1) << 2; // accommodate offset of sector offset table
    sectorOffsets = handle.archive.slice(0, sectorTableSize);
    handle.decoded.ensureCapacity(numSectors); // allows unsafe methods used in decoding

    // TODO: double check, initial might be 1 off
    startSector = currentSector = offset / sectorSize;
    endSector = (offset + length + sectorSize - 1) / sectorSize;
    for (int i = 0, s = startSector; i <= s; i++) updateSectorOffsets();

    if (offset > 0) {
      saturateBuffer()
          .readerIndex(offset % sectorSize);
    }
  }

  @Override
  public void close() {
    try {
      releaseSectorOffsets();
      releaseBuffer();
    } finally {
      if (releaseOnClose) {
        handle.release();
      }
    }
  }

  @Override
  public int available() {
    return buffer.readableBytes();
  }

  public int readableBytes() {
    return limit - bytesRead;
  }

  void releaseSectorOffsets() {
    ReferenceCountUtil.safeRelease(sectorOffsets);
    sectorOffsets = null;
  }

  void releaseBuffer() {
    ReferenceCountUtil.safeRelease(buffer);
    buffer = null;
  }

  int decrypt(int value) {
    seed += ENCRYPTION.get(key & 0xff);
    value ^= (key + seed);
    seed += value + (seed << 5) + 3;
    key = (~key << 0x15) + 0x11111111 | key >>> 0x0b;
    return value;
  }

  void updateSectorOffsets() {
    sectorOffset = nextSectorOffset;
    nextSectorOffset = sectorOffsets.readIntLE();
    if (encrypted) nextSectorOffset = decrypt(nextSectorOffset);
  }

  @Override
  public int read() {
    if (readableBytes() <= 0) {
      return -1;
    }

    if (!buffer.isReadable()) saturateBuffer();
    bytesRead++;
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", 1, available());
    return buffer.readUnsignedByte();
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    if (len > readableBytes()) {
      throw new IndexOutOfBoundsException(
          String.format(
              "bytesRead(+0x%x) + length(0x%x) exceeds declared limit(0x%x)",
              bytesRead, len, limit));
    }

    int bytesRead = 0;
    final ByteBuf dst = Unpooled.wrappedBuffer(b, off, len).clear();
    while (dst.isWritable() && readableBytes() > 0) {
      if (!buffer.isReadable()) saturateBuffer();
      final int writableBytes = Math.min(dst.writableBytes(), buffer.readableBytes());
      if (DEBUG_MODE) log.trace("Copying {} bytes", writableBytes);
      bytesRead += writableBytes;
      buffer.readBytes(dst, writableBytes);
    }

    this.bytesRead += bytesRead;
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", bytesRead, readableBytes());
    return bytesRead;
  }

  ByteBuf saturateBuffer() {
    assert currentSector < endSector : "currentSector(" + currentSector + ") >= endSector(" + endSector + ")";
    assert handle.buffer == null : "cannot stream mpq file handle if it has already been buffered";
    updateSectorOffsets();
    final int sectorSize = handle.mpq.sectorSize;
    final int bufferOffset = currentSector * sectorSize;
    final int sectorCSize = nextSectorOffset - sectorOffset;
    final int sectorFSize = Math.min(handle.FSize - bufferOffset, sectorSize);
    return DecoderExecutorGroup.SectorDecodeTask.decodeSync(
        handle,
        currentSector++,
        sectorOffset,
        sectorCSize,
        sectorFSize,
        buffer.clear(),
        0);
  }
}
