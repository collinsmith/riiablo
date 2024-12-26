package com.riiablo.map5;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Pool;

import com.riiablo.map5.util.BucketPool;

import static com.riiablo.map5.Tile.SUBTILE_HEIGHT;
import static com.riiablo.map5.Tile.SUBTILE_WIDTH;

public class Block {
  private Block() {}

  static final BucketPool<Block[]> pool = BucketPool
      .builder(Block[].class)
      .add(Tile.NUM_SUBTILES)
      .build();

  static Block[] obtain(int length) {
    Block[] blocks = pool.obtain(length);
    if (blocks[0] == null) {
      for (int i = 0, s = blocks.length; i < s; i++) {
        blocks[i] = blocks[i] == null ? new Block() : blocks[i];
      }
    }
    return blocks;
  }

  static void free(Block[] blocks) {
    pool.free(blocks);
  }

  static final int ISO_WIDTH = SUBTILE_WIDTH;
  static final int ISO_HEIGHT = SUBTILE_HEIGHT;
  static final int ISO_SIZE = (ISO_WIDTH * ISO_HEIGHT) / 2; // 256

  static final int RLE_WIDTH = 32;
  static final int RLE_HEIGHT = 32;
  static final int RLE_SIZE = RLE_WIDTH * RLE_HEIGHT * 3 / 2; // 1024 * 1.5 to account for rle compression overhead

  static final int ISO_FORMAT = 0x0001;
  static final int ISO_RLE_FORMAT = 0x2005;
  static final int RLE_FORMAT = 0x1001;

  static final String formatToString(short format) {
    switch (format) {
      case ISO_FORMAT: return "ISO_FORMAT";
      case ISO_RLE_FORMAT: return "ISO_RLE_FORMAT";
      case RLE_FORMAT: return "RLE_FORMAT";
      default: return String.format("0x%04x", format);
    }
  }

  static final BucketPool<byte[]> dataPool = BucketPool
      .builder(byte[].class)
      .add(ISO_SIZE)
      .add(RLE_SIZE)
      .build();

  static final Pool<byte[]> isoData = dataPool.get(ISO_SIZE);
  static final Pool<byte[]> rleData = dataPool.get(RLE_SIZE);

  byte[] obtainData(int length) {
    switch (format) {
      case ISO_FORMAT:
        assert length == ISO_SIZE : "length(" + length + ") != ISO_SIZE(" + ISO_SIZE + ")";
        return isoData.obtain();
      case ISO_RLE_FORMAT:
      case RLE_FORMAT:
        return dataPool.obtain(length);
      default:
        throw new AssertionError("Unsupported format: " + formatToString(format));
    }
  }

  void release(byte[] data) {
    switch (format) {
      case ISO_FORMAT:
        isoData.free(data);
      case ISO_RLE_FORMAT:
      case RLE_FORMAT:
        dataPool.free(data);
        break;
      default:
        throw new AssertionError("Unsupported format: " + formatToString(format));
    }
  }

  short x;
  short y;
  short xGrid; // iso = relative to SE subtile
  short yGrid; // iso = relative to SE subtile
  short format;
  int dataOffset;
  int dataLength;

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("x", x)
        .append("y", y)
        .append("xGrid", xGrid)
        .append("yGrid", yGrid)
        .append("format", formatToString(format))
        .append("dataLength", String.format("0x%x", dataLength))
        .append("dataOffset", String.format("+0x%08x", dataOffset))
        .toString();
  }
}
