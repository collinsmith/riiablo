package com.riiablo.map5;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteInputStream;
import com.riiablo.io.InvalidFormat;
import com.riiablo.io.UnsafeNarrowing;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.map5.util.BucketPool;

import static com.riiablo.map5.Block.ISO_FORMAT;
import static com.riiablo.map5.Block.ISO_RLE_FORMAT;
import static com.riiablo.map5.Block.RLE_FORMAT;
import static com.riiablo.map5.Orientation.FLOOR;
import static com.riiablo.map5.Orientation.ROOF;
import static com.riiablo.map5.Tile.NUM_SUBTILES;
import static com.riiablo.map5.Tile.SUBTILE_SIZE;

public enum Dt1Decoder7 {
  INSTANCE;

  private static final Logger log = LogManager.getLogger(Dt1Decoder7.class);

  private static final BucketPool<byte[]> pool = BucketPool.builder(byte[].class)
      .add(Tile.WIDTH * Tile.HEIGHT)
      .add(256 * 256)
      .build();

  public static final int VERSION = 0x7;

  static final int HEADER_SIZE = 0x114;
  static final int RESERVED_BYTES = 0x104;
  static final int TILE_HEADER_SIZE = 0x60;
  static final int BLOCK_HEADER_SIZE = 0x14;

  /*
   * Library Header
   * All Tile Headers
   * All Tile Blocks (block headers and then block datas)
   */

  public Dt1 decode(FileHandle handle, InputStream in) {
    return decode(handle, ByteInputStream.wrap(in, 0, (int) handle.length()));
  }

  public Dt1 decode(FileHandle handle, ByteInputStream in) {
    Dt1 dt1 = Dt1.obtain(handle);
    try {
      MDC.put("dt1", dt1.handle.path());
      dt1.version = in.readSafe32u();
      log.trace("dt1.version: {}", dt1.version);
      if (dt1.version != VERSION) {
        throw new InvalidFormat(
            in,
            String.format("Unsupported version %d, expected %d",
                dt1.version, VERSION));
      }
      return readHeaders(dt1, in);
    } catch (UnsafeNarrowing t) {
      throw new InvalidFormat(in, t);
    } finally {
      MDC.remove("dt1");
    }
  }

  public static Dt1 readHeaders(Dt1 dt1, ByteInputStream in) {
    readLibHeader(dt1, in);
    log.trace("dt1: {}", dt1);
    Tile[] tiles = dt1.tiles;
    for (int i = 0, s = dt1.numTiles; i < s; i++) {
      try {
        MDC.put("tile", i);
        readTileHeader(tiles[i] = Tile.obtain(), in);
        log.trace("tile: {}", tiles[i]);
      } finally {
        MDC.remove("tile");
      }
    }
    return dt1;
  }

  public static Dt1 readLibHeader(Dt1 dt1, ByteInputStream in) {
    assert dt1.version == VERSION : "dt1.version(" + dt1.version + ") != VERSION(" + VERSION + ")";
    dt1.flags = in.read32();
    in.skipBytes(RESERVED_BYTES);
    dt1.numTiles = in.readSafe32u();
    dt1.tiles = new Tile[dt1.numTiles];
    dt1.tileOffset = in.readSafe32u();
    return dt1;
  }

  public static Tile readTileHeader(Tile tile, ByteInputStream in) {
    tile.lightDirection = in.readSafe32u();
    tile.roofHeight = in.read16u();
    tile.materialFlags = in.read16();
    tile.height = in.read32();
    tile.width = in.read32();
    tile.heightToBottom = in.read32();
    tile.orientation = in.readSafe32u();
    tile.mainIndex = in.readSafe32u();
    tile.subIndex = in.readSafe32u();
    tile.updateIndex();
    tile.rarityFrame = in.readSafe32u();
    int transparentColorRgb24 = in.read32();
    log.tracef("transparentColorRgb24: 0x%08x", transparentColorRgb24);
    readFlags(tile, in); // 25 bytes
    in.skipBytes(7); // align to 32 bytes
    tile.blocksOffset = in.readSafe32u();
    tile.blocksLength = in.readSafe32u();
    tile.numBlocks = in.readSafe32u();
    in.skipBytes(4); // pointer, zeros
    short unk0x58 = in.read16();
    log.tracef("unk0x58: 0x%04x", unk0x58);
    tile.cacheIndex = in.read16();
    int unk0x5c = in.read32();
    log.tracef("unk0x5c: 0x%08x", unk0x5c);
    return tile;
  }

  static void readFlags(Tile tile, ByteInputStream in) {
    // read in reverse row order to correct coordinate mismatch
    byte[] flags = tile.flags;
    for (int i = NUM_SUBTILES - SUBTILE_SIZE; i >= 0; i -= SUBTILE_SIZE) {
      in.readBytes(flags, i, SUBTILE_SIZE);
    }
  }

  @Deprecated
  public static void decodeBlocks(Dt1 dt1, ByteInput in) {
    try {
      for (int i = 0, s = dt1.numTiles; i < s; i++) {
        MDC.put("tile", i);
        decodeBlocks(dt1, i, in);
      }
    } finally{
      MDC.remove("tile");
    }
  }

  public static void decodeBlocks(
      Dt1 dt1,
      int tile,
      ByteInput in
  ) {
    try {
      MDC.put("tile", tile);
      Tile t = dt1.tiles[tile];
      int width = t.width, height = -t.height;

      if (Orientation.isSpecial(t.orientation)) {
        width = t.width; // 160 to enforce default tile size?
      }

      int yOffs = Tile.WALL_HEIGHT;
      if (t.orientation == FLOOR || t.orientation == ROOF) {
        if (height != 0) {
          height = 80;
          yOffs = 0;
        }
      } else if (t.orientation < ROOF) {
        if (height != 0) {
          height += 32;
          yOffs = height;
        }
      }

      log.trace("t.size: {}x{}", width, height);
      log.trace("t.numBlocks: {}", t.numBlocks);
      byte[] pixmap = pool.obtain(width * height);
      try {
        decodeBlocks(dt1, t, in, pixmap, width, height, yOffs);
      } finally {
        pool.free(pixmap);
      }
    } finally {
      MDC.remove("tile");
    }
  }

  private static void decodeBlocks(
      Dt1 dt1,
      Tile t,
      ByteInput in,
      byte[] pixmap,
      int width,
      int height,
      int blockOffsY
  ) {
    Block[] blocks = Block.obtain(t.numBlocks);
    try {
      readBlockHeaders(blocks, t.numBlocks, in);
      readBlockData(blocks, t.numBlocks, in, pixmap, width, blockOffsY);
      t.pixmap = new PaletteIndexedPixmap(width, height, pixmap, width * height);
    } finally {
      Block.free(blocks);
    }
  }

  static void readBlockHeaders(Block[] blocks, int numBlocks, ByteInput in) {
    try {
      for (int i = 0, s = numBlocks; i < s; i++) {
        MDC.put("block", i);
        Block block = blocks[i];
        readBlockHeader(block, in);
        log.trace("block: {}", block);
      }
    } finally {
      MDC.remove("block");
    }
  }

  static void readBlockHeader(Block block, ByteInput in) {
    block.x = in.read16();
    block.y = in.read16();
    short unk0x04 = in.read16();
    block.xGrid = in.read8u();
    block.yGrid = in.read8u();
    block.format = in.read16();
    // known format
    block.dataLength = in.readSafe32u();
    short unk0x0e = in.read16();
    block.dataOffset = in.readSafe32u();
    log.tracef("unk0x04: 0x%04x", unk0x04);
    log.tracef("unk0x0e: 0x%04x", unk0x0e);
  }

  static void readBlockData(
      Block[] blocks,
      int numBlocks,
      ByteInput in,
      byte[] pixmap,
      int pixmapWidth,
      int blockOffsY
  ) {
    try {
      for (int i = 0, s = numBlocks; i < s; i++) {
        MDC.put("block", i);
        Block block = blocks[i];
        byte[] data = block.obtainData(block.dataLength);
        try {
          in.readBytes(data, 0, block.dataLength);
          decodeBlockData(block, data, pixmap, pixmapWidth, blockOffsY);
        } finally {
          block.release(data);
        }
      }
    } finally {
      MDC.remove("block");
    }

  }

  static void decodeBlockData(
      Block block,
      byte[] data,
      byte[] pixmap,
      int pixmapWidth,
      int blockOffsY
  ) {
    switch (block.format) {
      case ISO_FORMAT:
        decodeIsoBlock(pixmap, pixmapWidth, block.x, blockOffsY + block.y, data);
        break;
      case ISO_RLE_FORMAT:
      case RLE_FORMAT:
        decodeRleBlock(pixmap, pixmapWidth, block.x, blockOffsY + block.y, data, block.dataLength);
        break;
      default:
        throw new AssertionError("Unsupported block format: " + Block.formatToString(block.format));
    }
  }

  private static final int[] SKIP = { 14, 12, 10, 8, 6, 4, 2, 0, 2, 4, 6, 8, 10, 12, 14 };
  private static final int[] RUN = { 4, 8, 12, 16, 20, 24, 28, 32, 28, 24, 20, 16, 12, 8, 4 };

  static void decodeIsoBlock(
      final byte[] pixmap,
      final int pixmapWidth,
      final int x0,
      final int y0,
      final byte[] data
  ) {
    final int[] SKIP = Dt1Decoder7.SKIP;
    final int[] RUN = Dt1Decoder7.RUN;
    int length = 0x100;
    int skip, run, i = 0;
    int offs = y0 * pixmapWidth;
    for (int y = 0; length > 0; y++, length -= run, offs += pixmapWidth, i += run) {
      skip = offs + x0 + SKIP[y];
      run = RUN[y];
      System.arraycopy(data, i, pixmap, skip, run);
    }
  }

  static void decodeRleBlock(
      final byte[] pixmap,
      final int pixmapWidth,
      final int x0,
      final int y0,
      final byte[] data,
      int length
  ) {
    int run, len, i = 0;
    int x = x0;
    int offs = y0 * pixmapWidth;
    for (; length > 0; length -= 2) {
      run = data[i++]; // [0u-32u]
      len = data[i++]; // [0u-32u]
      if (run == 0 && len == 0) {
        x = x0;
        offs += pixmapWidth;
      } else {
        x += run;
        System.arraycopy(data, i, pixmap, offs + x, len);
        i += len;
        x += len;
        length -= len;
      }
    }
  }
}
