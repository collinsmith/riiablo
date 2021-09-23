package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.codec.Palette;
import com.riiablo.codec.util.BBox;
import com.riiablo.io.BitConstraints;
import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public final class Dcc extends Dc<Dcc.DccDirection> {
  private static final Logger log = LogManager.getLogger(Dcc.class);

  static final int ENCODED_BITS[] = {
      0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 20, 24, 26, 28, 30, 32
  };

  static int readSafe32u(final BitInput in, final int bits) {
    long value = in.read63u(ENCODED_BITS[bits]);
    return BitConstraints.safe32u(in.byteMark(), value);
  }

//final FileHandle handle; // Dc#handle
  final byte[] signature;
  final byte version;
//final int numDirections; // Dc#numDirections
//final int numFrames; // Dc#numFrames
//final int dirOffsets; // Dc#dirOffsets;

  public static Dcc read(FileHandle handle, InputStream stream) {
    SwappedDataInputStream in = new SwappedDataInputStream(stream);
    try {
      return read(handle, in);
    } catch (Throwable t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public static Dcc read(FileHandle handle, SwappedDataInputStream in) throws IOException {
    byte[] signature = IOUtils.readFully(in, 1);
    if (log.traceEnabled()) log.trace("signature: {}", ByteBufUtil.hexDump(signature));
    byte version = in.readByte();
    log.trace("version: {}", version);
    int numDirections = in.readByte();
    log.trace("numDirections: {}", numDirections);
    int numFrames = in.readInt();
    log.trace("numFrames: {}", numFrames);
    int tag = in.readInt();
    log.tracef("tag: 0x%08x", tag);
    int uncompressedSize = in.readInt();
    log.trace("uncompressedSize: {} bytes", uncompressedSize);

    final int[] dirOffsets = new int[numDirections + 1];
    for (int i = 0, s = numDirections; i < s; i++) dirOffsets[i] = in.readInt();
    dirOffsets[numDirections] = (int) handle.length();
    log.trace("dirOffsets: {}", dirOffsets);

    return new Dcc(handle, signature, version, numDirections, numFrames, dirOffsets);
  }

  Dcc(
      FileHandle handle,
      byte[] signature,
      byte version,
      int numDirections,
      int numFrames,
      int[] dirOffsets
  ) {
    super(handle, numDirections, numFrames, dirOffsets, DccDirection.class);
    this.signature = signature;
    this.version = version;
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  public Dcc read(ByteBuf buffer, int direction) {
    assert directions[direction] == null;
    assert buffer.isReadable(dirOffsets[direction + 1] - dirOffsets[direction]);
    ByteInput in = ByteInput.wrap(buffer);
    directions[direction] = new DccDirection(in.unalign(), numFrames);
    return this;
  }

  public static final class DccDirection implements Dc.Direction<DccFrame> {
    static final int HasRawPixelEncoding = 1 << 0;
    static final int CompressEqualCells = 1 << 1;

    public static String getFlagsString(byte compressionFlags) {
      StringBuilder builder = new StringBuilder(16);
      if ((compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding) {
        builder.append("HasRawPixelEncoding|");
      }
      if ((compressionFlags & CompressEqualCells) == CompressEqualCells) {
        builder.append("CompressEqualCells|");
      }
      if (builder.length() > 0) builder.setLength(builder.length() - 1);
      return builder.toString();
    }

    // Dc
    final DccFrame[] frames;
    final BBox box;

    // Dcc
    final int uncompressedSize;
    final byte compressionFlags;
    final byte variable0Bits;
    final byte widthBits;
    final byte heightBits;
    final byte xOffsetBits;
    final byte yOffsetBits;
    final byte optionalBytesBits;
    final byte compressedBytesBits;

    final long equalCellBitStreamSize;
    final long pixelMaskBitStreamSize;
    final long encodingTypeBitStreamSize;
    final long rawPixelCodesBitStreamSize;

    final BitInput equalCellBitStream;
    final BitInput pixelMaskBitStream;
    final BitInput encodingTypeBitStream;
    final BitInput rawPixelCodesBitStream;
    final BitInput pixelCodeAndDisplacementBitStream;

    final byte[] pixelValues;

    DccDirection(BitInput bits, int numFrames) {
      uncompressedSize = bits.readSafe32u();
      log.trace("uncompressedSize: {} bytes", uncompressedSize);

      compressionFlags = (byte) bits.readRaw(2);
      if (log.traceEnabled()) log.tracef("compressionFlags: 0x%01x (%s)", compressionFlags, DccDirection.getFlagsString(compressionFlags));

      variable0Bits = bits.read7u(4);
      log.trace("variable0Bits: {} ({} bits)", variable0Bits, ENCODED_BITS[variable0Bits]);

      widthBits = bits.read7u(4);
      log.trace("widthBits: {} ({} bits)", widthBits, ENCODED_BITS[widthBits]);

      heightBits = bits.read7u(4);
      log.trace("heightBits: {} ({} bits)", heightBits, ENCODED_BITS[heightBits]);

      xOffsetBits = bits.read7u(4);
      log.trace("xOffsetBits: {} ({} bits)", xOffsetBits, ENCODED_BITS[xOffsetBits]);

      yOffsetBits = bits.read7u(4);
      log.trace("yOffsetBits: {} ({} bits)", yOffsetBits, ENCODED_BITS[yOffsetBits]);

      optionalBytesBits = bits.read7u(4);
      log.trace("optionalBytesBits: {} ({} bits)", optionalBytesBits, ENCODED_BITS[optionalBytesBits]);

      compressedBytesBits = bits.read7u(4);
      log.trace("compressedBytesBits: {} ({} bits)", compressedBytesBits, ENCODED_BITS[compressedBytesBits]);

      box = new BBox().prepare();
      DccFrame[] frames = this.frames = new DccFrame[numFrames];

      long codedBytes = 0;
      long optionalBytes = 0;
      for (int frame = 0; frame < numFrames; frame++) {
        try {
          MDC.put("frame", frame);
          DccFrame f = frames[frame] = new DccFrame(
              bits,
              variable0Bits,
              widthBits,
              heightBits,
              xOffsetBits,
              yOffsetBits,
              optionalBytesBits,
              compressedBytesBits);
          box.max(f.box);
          codedBytes += f.compressedBytes;
          optionalBytes += f.optionalBytes;
        } finally {
          MDC.remove("frame");
        }
      }

      log.trace("compressedBytes: {} bytes", codedBytes);
      log.trace("optionalBytes: {} bytes", optionalBytes);

      box.width++; // why?
      box.height++; // why?

      if (optionalBytes > 0) {
        for (int frame = 0; frame < numFrames; frame++) {
          DccFrame f = frames[frame];
          if (f.optionalBytes > 0) {
            f.optionalData = bits.align().readSlice(f.optionalBytes);
          }
        }
      }

      equalCellBitStreamSize = (compressionFlags & CompressEqualCells) == CompressEqualCells ? bits.read31u(20) : 0;
      log.trace("equalCellBitStreamSize: {} bits", equalCellBitStreamSize);
      pixelMaskBitStreamSize = bits.read31u(20);
      log.trace("pixelMaskBitStreamSize: {} bits", pixelMaskBitStreamSize);
      encodingTypeBitStreamSize = (compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding ? bits.read31u(20) : 0;
      log.trace("encodingTypeBitStreamSize: {} bits", encodingTypeBitStreamSize);
      rawPixelCodesBitStreamSize = (compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding ? bits.read31u(20) : 0;
      log.trace("rawPixelCodesBitStreamSize: {} bits", rawPixelCodesBitStreamSize);

      pixelValues = new byte[Palette.COLORS];
      readPixelValues(bits, pixelValues);

      assert (compressionFlags & CompressEqualCells) != CompressEqualCells || equalCellBitStreamSize > 0;
      equalCellBitStream = bits.readSlice(equalCellBitStreamSize);
      pixelMaskBitStream = bits.readSlice(pixelMaskBitStreamSize);
      assert (compressionFlags & HasRawPixelEncoding) != HasRawPixelEncoding
          || (encodingTypeBitStreamSize > 0 && rawPixelCodesBitStreamSize > 0);
      encodingTypeBitStream = bits.readSlice(encodingTypeBitStreamSize);
      rawPixelCodesBitStream = bits.readSlice(rawPixelCodesBitStreamSize);
      pixelCodeAndDisplacementBitStream = bits.readSlice(bits.bitsRemaining());
    }

    // TODO: benchmark and see if this requires optimization -- 256 single bit reads
    private static void readPixelValues(BitInput bits, byte[] pixelValues) {
      int index = 0;
      for (int i = 0; i < Palette.COLORS; i++) {
        if (bits.readBoolean()) pixelValues[index++] = (byte) i;
      }
    }

    @Override
    public DccFrame[] frames() {
      return frames;
    }

    @Override
    public DccFrame frame(int f) {
      return frames[f];
    }

    @Override
    public BBox box() {
      return box;
    }
  }

  public static final class DccFrame implements Dc.Frame {
    // Dc
    final byte flip;
    final int width;
    final int height;
    final int xOffset;
    final int yOffset;
    final int variable0;

    final BBox box;

    // Dcc
    final int compressedBytes;
    final int optionalBytes;
    ByteInput optionalData;

    DccFrame(
        BitInput bits,
        byte variable0Bits,
        byte widthBits,
        byte heightBits,
        byte xOffsetBits,
        byte yOffsetBits,
        byte optionalBytesBits,
        byte compressedBytesBits
    ) {
      variable0 = readSafe32u(bits, variable0Bits);
      log.trace("variable0: {}", variable0);

      width = readSafe32u(bits, widthBits);
      log.trace("width: {}", width);

      height = readSafe32u(bits, heightBits);
      log.trace("height: {}", height);

      xOffset = readSafe32u(bits, xOffsetBits);
      log.trace("xOffset: {}", xOffset);

      yOffset = readSafe32u(bits, yOffsetBits);
      log.trace("yOffset: {}", yOffset);

      optionalBytes = readSafe32u(bits, optionalBytesBits);
      log.trace("optionalBytes: {} bytes", optionalBytes);

      compressedBytes = readSafe32u(bits, compressedBytesBits);
      log.trace("compressedBytes: {} bytes", compressedBytes);

      flip = bits.read1();
      log.trace("flip: {}", flip);

      box = new BBox();
      box.xMin = xOffset;
      box.xMax = box.xMin + width - 1;
      if (flip != 0) { // bottom-up
        box.yMin = yOffset;
        box.yMax = box.yMin + height - 1;
      } else {        // top-down
        box.yMax = yOffset;
        box.yMin = box.yMax - height + 1;
      }

      box.width = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;
    }

    @Override
    public byte flip() {
      return flip;
    }

    @Override
    public int width() {
      return width;
    }

    @Override
    public int height() {
      return height;
    }

    @Override
    public int xOffset() {
      return xOffset;
    }

    @Override
    public int yOffset() {
      return yOffset;
    }

    @Override
    public BBox box() {
      return box;
    }
  }
}
