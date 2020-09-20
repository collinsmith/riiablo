package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FilenameUtils;

import com.badlogic.gdx.utils.Pool;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.MPQ.Block;
import com.riiablo.mpq_bytebuf.util.Decompressor;
import com.riiablo.mpq_bytebuf.util.Decryptor;
import com.riiablo.mpq_bytebuf.util.Exploder;

import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_COMPRESSED;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_ENCRYPTED;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_EXISTS;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_FIX_KEY;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_IMPLODE;

public final class MPQInputStream {
  private static final Logger log = LogManager.getLogger(MPQInputStream.class);

  private static final boolean DEBUG_MODE = !true;

  private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

  private static final int COMPRESSED_OR_IMPLODE = FLAG_COMPRESSED | FLAG_IMPLODE;

  private static final int MAX_SECTORS = 1024;
  private static final Pool<int[]> INTS = new Pool<int[]>(8, 32, true) {
    @Override
    protected int[] newObject() {
      return new int[MAX_SECTORS];
    }
  };
  private static final Pool<byte[]> BYTES = new Pool<byte[]>(8, 32, true) {
    @Override
    protected byte[] newObject() {
      return new byte[MAX_SECTORS << 2];
    }
  };

  private MPQInputStream() {}

  public static ByteBuf readByteBuf(final MPQ mpq, final String filename, final Block block) {
    final int flags = block.flags;
    assert (flags & FLAG_EXISTS) == FLAG_EXISTS;
    final int offset = block.offset;
    final int CSize = block.CSize;
    final int FSize = block.FSize;

    final ByteBuf archive = mpq.buffer().readerIndex(offset);
    if ((flags & ~FLAG_EXISTS) == 0) {
      assert CSize == FSize;
      return archive.readRetainedSlice(FSize);
    }

    final int sectorSize = mpq.sectorSize;
    final int sectorCount = (FSize + sectorSize - 1) / sectorSize;
    final CompositeByteBuf buffer = ALLOC.compositeHeapBuffer();

    if (DEBUG_MODE) log.trace("flags=" + block.getFlagsString());
    assert (flags & COMPRESSED_OR_IMPLODE) != 0;

    if (DEBUG_MODE) log.trace("Populating sector offsets table...");
    final byte[] sectorOffsetBytes = BYTES.obtain();
    final int[] sectorOffsets = INTS.obtain();
    try {
      final int encryptionKey = getEncryptionKey(filename, flags, offset, FSize);
      final ByteBuf sectorOffsetBuffer = Unpooled.wrappedBuffer(sectorOffsetBytes)
          .clear()
          .writeBytes(archive, sectorCount << 2);
      if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
        if (DEBUG_MODE) log.trace("Decrypting sector offsets table...");
        Decryptor.decrypt(encryptionKey - 1, sectorOffsetBuffer);
      }

      for (int i = 0, s = sectorCount; i < s; i++) {
        sectorOffsets[i] = MPQ.readSafeUnsignedIntLE(sectorOffsetBuffer);
      }

      sectorOffsets[sectorCount] = CSize;

      if (DEBUG_MODE && log.traceEnabled()) {
        final StringBuilder builder = new StringBuilder(256);
        for (int i = 0, s = sectorCount; i <= s; i++) {
          builder.append(Integer.toHexString(sectorOffsets[i])).append(',');
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        log.trace("sector offsets: {}+[{}]", Integer.toHexString(offset), builder);
      }

      for (int i = 0, s = sectorCount; i <= s; i++) {
        sectorOffsets[i] += offset;
      }

      int decompressedBytes = 0;
      for (int curSector = 0, s = sectorCount; curSector < s; curSector++) {
        final int sectorCSize = sectorOffsets[curSector + 1] - sectorOffsets[curSector];
        final int sectorFSize = Math.min(FSize - decompressedBytes, sectorSize);
        log.debug("Reading sector {} / {} ({} bytes)", curSector, s - 1, sectorCSize);
        archive.readerIndex(sectorOffsets[curSector]);

        final ByteBuf sector = ALLOC.heapBuffer(sectorFSize);
        if (DEBUG_MODE) log.trace("sector: {}", sector);
        final ByteBuf slice = archive.readSlice(sectorCSize);
        if (DEBUG_MODE) log.trace("slice: {}", slice);

        if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
          if (DEBUG_MODE) log.trace("Decrypting sector...");
          Decryptor.decrypt(encryptionKey + curSector, slice, sector);
          if (DEBUG_MODE) log.trace("Decrypted {} bytes", sector.writerIndex());
        } else {
          sector.writeBytes(slice);
        }

        if ((flags & FLAG_COMPRESSED) == FLAG_COMPRESSED && sectorCSize != sectorSize) {
          if (DEBUG_MODE) log.trace("Decompressing sector...");
          Decompressor.decompress(sector, sectorCSize, sectorFSize);
          if (DEBUG_MODE) log.trace("Decompressed {} bytes", sector.writerIndex());
        }

        if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE && sectorCSize != sectorSize) {
          if (DEBUG_MODE) log.trace("Exploding sector...");
          Exploder.pkexplode(sector);
          if (DEBUG_MODE) log.trace("Exploded {} bytes", sector.writerIndex());
        }

        buffer.addComponent(true, sector);
      }
    } finally {
      BYTES.free(sectorOffsetBytes);
      INTS.free(sectorOffsets);
    }

    return buffer;
  }

  private static int getEncryptionKey(final String filename, final int flags, final int offset, final int FSize) {
    if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
      final String basename = FilenameUtils.getName(filename);
      final int encryptionKey = Decryptor.HASH_ENCRYPTION_KEY.hash(basename);
      return (flags & FLAG_FIX_KEY) == FLAG_FIX_KEY
          ? (encryptionKey + offset) ^ FSize
          : encryptionKey;
    }

    return 0;
  }
}
