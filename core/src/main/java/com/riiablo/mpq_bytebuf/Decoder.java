package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.mpq_bytebuf.util.ADPCM;
import com.riiablo.mpq_bytebuf.util.Exploder;
import com.riiablo.mpq_bytebuf.util.Huffman;

public final class Decoder {
  private static final Logger log = LogManager.getLogger(Decoder.class);

  /* Masks for Decompression Type 2 */
  static final int FLAG_HUFFMAN = 0x01;
  static final int FLAG_DEFLATE = 0x02;
  static final int FLAG_UNK04   = 0x04;
  static final int FLAG_IMPLODE = 0x08;
  static final int FLAG_BZIP2   = 0x10;
  static final int FLAG_SPARSE  = 0x20;
  static final int FLAG_ADPCM1C = 0x40;
  static final int FLAG_ADPCM2C = 0x80;
  static final int FLAG_LMZA    = 0x12;
  static final int ADPCM_MASK = FLAG_ADPCM1C | FLAG_ADPCM2C;
  static final int UNSUPPORTED_FLAGS_MASK = FLAG_DEFLATE | FLAG_LMZA | FLAG_BZIP2 | FLAG_SPARSE;

  static String getFlagsString(int flags) {
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

  final Huffman huffmanDecoder = new Huffman();

  public void explode(final ByteBuf in, final ByteBuf out) {
    log.trace("Exploding {}...", in);
    Exploder.explode(in, out);
    log.trace("Exploded {} bytes", out.writerIndex());
  }

  public void decodeHuffman(final ByteBuf in, final ByteBuf out) {
    log.trace("Huffman decoding {}...", in);
    huffmanDecoder.inflate(in, out);
    log.trace("Huffman inflated {} bytes", out.writerIndex());
  }

  public void decodeAdpcm(final int numChannels, final ByteBuf in, final ByteBuf out) {
    log.trace("PCM{}C decompressing {}...", numChannels, in);
    ADPCM.decode(in, out, numChannels);
    log.trace("PCM{}C decompressed {} bytes", numChannels, out.writerIndex());
  }

  public void decode(
      ByteBuf in,
      final ByteBuf out,
      ByteBuf scratch,
      final int CSize,
      final int FSize
  ) {
    final int flags = in.readUnsignedByte();
    decode(flags, in, out, scratch, CSize, FSize);
  }

  public void decode(
      int flags,
      ByteBuf in,
      final ByteBuf out,
      ByteBuf scratch,
      final int CSize,
      final int FSize
  ) {
    assert CSize != FSize : "CSize(" + CSize + ") == FSize(" + FSize + ")";

    if (log.debugEnabled()) log.debug("flags: {}", getFlagsString(flags));
    if ((flags & UNSUPPORTED_FLAGS_MASK) != 0) {
      throw new UnsupportedOperationException(
          String.format("Unsupported compression flag: %s",
              getFlagsString(flags & UNSUPPORTED_FLAGS_MASK)));
    }

    // fast decode directly into output buffer
    if (flags == FLAG_IMPLODE) {
      explode(in, out);
      return;
    }

    // alternate between decoding #in and #tmpOut and copy final #tmpOut into #out
    boolean alternate = false;
    if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE) {
      // if (alternate) { ByteBuf t = in; in = tmpOut; tmpOut = t; }
      explode(in, scratch.clear());
      alternate = true;
    }

    if ((flags & FLAG_HUFFMAN) == FLAG_HUFFMAN) {
      if (alternate) { ByteBuf t = in; in = scratch; scratch = t; }
      decodeHuffman(in, scratch.clear());
      alternate = true;
    }

    if ((flags & ADPCM_MASK) != 0) {
      if (alternate) { ByteBuf t = in; in = scratch; scratch = t; }
      final int numChannels = ((flags & FLAG_ADPCM1C) == FLAG_ADPCM1C) ? ADPCM.MONO : ADPCM.STEREO;
      decodeAdpcm(numChannels, in, scratch.clear());
      // alternate = true;
    }

    out.writeBytes(scratch);
  }
}
