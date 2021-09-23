package com.riiablo.file;

import io.netty.buffer.ByteBuf;
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
    byte version = in.readByte();
    int numDirections = in.readByte();
    int numFrames = in.readInt();
    int tag = in.readInt();
    int uncompressedSize = in.readInt();

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
    final byte extraBytesBits;
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
      compressionFlags = (byte) bits.readRaw(2);
      variable0Bits = bits.read7u(4);
      widthBits = bits.read7u(4);
      heightBits = bits.read7u(4);
      xOffsetBits = bits.read7u(4);
      yOffsetBits = bits.read7u(4);
      extraBytesBits = bits.read7u(4);
      compressedBytesBits = bits.read7u(4);

      box = new BBox().prepare();
      DccFrame[] frames = this.frames = new DccFrame[numFrames];

      long extraBytes = 0;
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
              extraBytesBits,
              compressedBytesBits);
          box.max(f.box);
          extraBytes += f.extraBytes;
        } finally {
          MDC.remove("frame");
        }
      }

      box.width++; // why?
      box.height++; // why?

      if (extraBytes > 0) {
        ByteInput in = bits.align();
        for (int frame = 0; frame < numFrames; frame++) {
          DccFrame f = frames[frame];
          if (f.extraBytes > 0) {
            f.extraData = in.readSlice(f.extraBytes); // TODO: 0 => EMPTY
          }
        }
      }

      equalCellBitStreamSize = (compressionFlags & CompressEqualCells) == CompressEqualCells ? bits.read31u(20) : 0;
      pixelMaskBitStreamSize = bits.read31u(20);
      encodingTypeBitStreamSize = (compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding ? bits.read31u(20) : 0;
      rawPixelCodesBitStreamSize = (compressionFlags & HasRawPixelEncoding) == HasRawPixelEncoding ? bits.read31u(20) : 0;

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
    final boolean flipY;
    final int width;
    final int height;
    final int xOffset;
    final int yOffset;
    final int variable0;

    final BBox box;

    // Dcc
    final int compressedBytes;
    final int extraBytes;
    ByteInput extraData = ByteInput.emptyByteInput();

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
      width = readSafe32u(bits, widthBits);
      height = readSafe32u(bits, heightBits);
      xOffset = readSafe32u(bits, xOffsetBits);
      yOffset = readSafe32u(bits, yOffsetBits);
      extraBytes = readSafe32u(bits, optionalBytesBits);
      compressedBytes = readSafe32u(bits, compressedBytesBits);
      flipY = bits.readBoolean();

      box = new BBox();
      box.xMin = xOffset;
      box.xMax = box.xMin + width - 1;
      if (flipY) {
        // bottom-up
        box.yMin = yOffset;
        box.yMax = box.yMin + height - 1;
      } else {
        // top-down
        box.yMax = yOffset;
        box.yMin = box.yMax - height + 1;
      }

      box.width = box.xMax - box.xMin + 1;
      box.height = box.yMax - box.yMin + 1;
    }

    @Override
    public boolean flipY() {
      return flipY;
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
