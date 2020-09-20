package com.riiablo.mpq_bytebuf.util;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.utils.Pool;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class Decompressor {
  private Decompressor() {}

  private static final Logger log = LogManager.getLogger(Decompressor.class);

  /* Masks for Decompression Type 2 */
  private static final int FLAG_HUFFMAN = 0x01;
  public  static final int FLAG_DEFLATE = 0x02;
  // 0x04 is unknown
  private static final int FLAG_IMPLODE = 0x08;
  private static final int FLAG_BZIP2   = 0x10;
  private static final int FLAG_SPARSE  = 0x20;
  private static final int FLAG_ADPCM1C = 0x40;
  private static final int FLAG_ADPCM2C = 0x80;
  private static final int FLAG_LMZA    = 0x12;
  private static final int ADPCM_MASK = FLAG_ADPCM1C | FLAG_ADPCM2C;
  private static final int UNSUPPORTED_FLAGS_MASK = FLAG_DEFLATE | FLAG_LMZA | FLAG_BZIP2 | FLAG_SPARSE;

  private static String getFlagsString(int flags) {
    if (flags == 0) return "0";
    StringBuilder builder = new StringBuilder(64);
    builder.append(StringUtils.leftPad(Integer.toHexString(flags), 8, '0'));
    builder.append(" (");
    final int startingLen = builder.length();
    if ((flags & FLAG_LMZA) == FLAG_LMZA) builder.append("LMZA|");
    if ((flags & FLAG_ADPCM2C) == FLAG_ADPCM2C) builder.append("ADPCM2C|");
    if ((flags & FLAG_ADPCM1C) == FLAG_ADPCM1C) builder.append("ADPCM1C|");
    if ((flags & FLAG_SPARSE) == FLAG_SPARSE) builder.append("SPARSE|");
    if ((flags & FLAG_BZIP2) == FLAG_BZIP2) builder.append("BZIP2|");
    if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE) builder.append("IMPLODE|");
    if ((flags & FLAG_DEFLATE) == FLAG_DEFLATE) builder.append("DEFLATE|");
    if ((flags & FLAG_HUFFMAN) == FLAG_HUFFMAN) builder.append("HUFFMAN|");
    if (builder.length() > startingLen) {
      builder.setCharAt(builder.length() - 1, ')');
    } else {
      builder.append(')');
    }

    return builder.toString();
  }

  private static final Pool<Huffman> HUFFMAN = new Pool<Huffman>(8, 32, true) {
    @Override
    protected Huffman newObject() {
      return new Huffman();
    }
  };

  public static void decompress(final ByteBuf inout, final int CSize, final int FSize) {
    log.traceEntry("decompress(inout: {}, CSize: {}, FSize: {})", inout, CSize, FSize);
    assert CSize != FSize;
    assert inout.readableBytes() == CSize;

    final int compressionFlags = inout.readUnsignedByte();
    inout.markReaderIndex();
    if (log.debugEnabled()) log.debug("compressionFlags: {}", getFlagsString(compressionFlags));
    if ((compressionFlags & UNSUPPORTED_FLAGS_MASK) != 0) {
      throw new UnsupportedOperationException(
          "Unsupported compression flag: " + getFlagsString(compressionFlags & UNSUPPORTED_FLAGS_MASK));
    } else if ((compressionFlags & FLAG_IMPLODE) == FLAG_IMPLODE) {
      log.trace("Exploding {}...", inout);
      Exploder.pkexplode(inout);
      log.trace("Exploded {} bytes", inout.writerIndex());
      inout.readerIndex(0).markReaderIndex();
    }

    if ((compressionFlags & FLAG_HUFFMAN) == FLAG_HUFFMAN) {
      final Huffman huffman = HUFFMAN.obtain();
      try {
        log.trace("Huffman decompressing {}...", inout);
        huffman.decompress(inout);
        log.trace("Huffman decompressed {} bytes", inout.writerIndex());
        inout.readerIndex(0).markReaderIndex();
      } finally {
        HUFFMAN.free(huffman);
      }
    }

    if ((compressionFlags & ADPCM_MASK) != 0) {
      final int numChannels = ((compressionFlags & FLAG_ADPCM1C) == FLAG_ADPCM1C) ? ADPCM.MONO : ADPCM.STEREO;
      log.trace("PCM{}C decompressing {}...", numChannels, inout);
      ADPCM.decompress(inout, numChannels);
      log.trace("PCM{}C decompressed {} bytes", numChannels, inout.writerIndex());
    }
  }
}
