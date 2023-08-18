package com.riiablo.file;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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

  static int readEncodedSafe32u(final BitInput in, final int bits) {
    long value = in.read63u(ENCODED_BITS[bits]);
    return BitConstraints.safe32u(in.byteMark(), value);
  }

  static int readEncoded32(final BitInput in, final int bits) {
    return in.read32(ENCODED_BITS[bits]);
  }

//final FileHandle handle; // Dc#handle
  final byte[] signature;
  final byte version;
//final int numDirections; // Dc#numDirections
//final int numFrames; // Dc#numFrames
  final int[] dirOffsets;
  final int tag;
  final int uncompressedSize;

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

    return new Dcc(handle, signature, version, numDirections, numFrames, dirOffsets, tag, uncompressedSize);
  }

  Dcc(
      FileHandle handle,
      byte[] signature,
      byte version,
      int numDirections,
      int numFrames,
      int[] dirOffsets,
      int tag,
      int uncompressedSize
  ) {
    super(handle, numDirections, numFrames, DccDirection.class);
    this.signature = signature;
    this.version = version;
    this.dirOffsets = dirOffsets;
    this.tag = tag;
    this.uncompressedSize = uncompressedSize;
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public int dirOffset(int d) {
    return dirOffsets[d];
  }

  @Override
  public BBox box(int d, int f) {
    return box(d);
  }

  @Override
  public Dcc read(ByteBuf buffer, int direction) {
    super.read(buffer, direction);
    ByteInput in = ByteInput.wrap(buffer);
    directions[direction] = new DccDirection(in.unalign(), numFrames);
    return this;
  }

  @Override
  public void uploadTextures(int d, int combineFrames) {
    if (combineFrames == 1) throw new UnsupportedOperationException("DCC do not support combined frames");
    final DccDirection direction = directions[d];
    final DccFrame[] frame = direction.frames;
    final Pixmap[] pixmap = direction.pixmap;
    final Texture[] texture = direction.texture;
    for (int f = 0; f < numFrames; f++) {
      // FIXME: memory leak if called multiple times, asserted asset manger works correctly
      Texture t = texture[f] = new Texture(pixmap[f]);
      frame[f].texture.setRegion(t);
      pixmap[f].dispose();
      pixmap[f] = null;
    }
  }

  public static final class DccDirection extends Dc.Direction<DccFrame> {
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
    final Pixmap[] pixmap;
    final Texture[] texture;

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

    final short[] pixelValues;

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
      pixmap = new Pixmap[numFrames];
      texture = new Texture[numFrames];
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

      pixelValues = new short[Palette.COLORS];
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
    private static void readPixelValues(BitInput bits, short[] pixelValues) {
      int index = 0;
      for (short i = 0; i < Palette.COLORS; i++) {
        if (bits.readBoolean()) pixelValues[index++] = i;
      }
    }

    @Override
    public void dispose() {
      log.trace("disposing dcc pixmaps");
      for (int i = 0, s = pixmap.length; i < s; i++) {
        if (pixmap[i] == null) continue;
        pixmap[i].dispose();
        pixmap[i] = null;
      }

      log.trace("disposing dcc textures");
      for (int i = 0, s = texture.length; i < s; i++) {
        if (texture[i] == null) continue;
        texture[i].dispose();
        texture[i] = null;
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

  public static final class DccFrame extends Dc.Frame {
    // Dc
    final boolean flipY;
    final int width;
    final int height;
    final int xOffset;
    final int yOffset;
    final int variable0;

    final BBox box;
    final TextureRegion texture;

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
      // Note: must look up actual number of bits to read from ENCODED_BITS
      variable0 = readEncodedSafe32u(bits, variable0Bits);
      width = readEncodedSafe32u(bits, widthBits);
      height = readEncodedSafe32u(bits, heightBits);
      xOffset = readEncoded32(bits, xOffsetBits); // signed
      yOffset = readEncoded32(bits, yOffsetBits); // signed
      extraBytes = readEncodedSafe32u(bits, optionalBytesBits);
      compressedBytes = readEncodedSafe32u(bits, compressedBytesBits);
      flipY = bits.readBoolean();
      box = new BBox().asBox(xOffset, flipY ? yOffset : yOffset - height, width, height);
      texture = MISSING_TEXTURE == null ? new TextureRegion() : new TextureRegion(MISSING_TEXTURE);
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

    @Override
    public TextureRegion texture() {
      return texture;
    }
  }
}
