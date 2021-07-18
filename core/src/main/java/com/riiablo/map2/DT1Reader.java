package com.riiablo.map2;

import java.util.Arrays;
import org.apache.commons.lang3.Conversion;

import com.riiablo.codec.util.BBox;
import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.DT1.Tile.Block;
import com.riiablo.util.ArrayUtils;
import com.riiablo.util.DebugUtils;

import static com.riiablo.map2.DT1.Tile.Block.ISO_FORMAT;
import static com.riiablo.map2.DT1.Tile.Block.ISO_RLE_FORMAT;
import static com.riiablo.map2.DT1.Tile.Block.RLE_FORMAT;

public class DT1Reader {
  private static final Logger log = LogManager.getLogger(DT1Reader.class);
  private static final boolean DEBUG_ZEROS = true;

  static final byte[] SIGNATURE = {0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00};
  static final int HEADER_SIZE = 0x114;
  static final int RESERVED = 0x104;
  static final int TILE_HEADER_SIZE = 0x60;
  static final int BLOCK_SIZE = 0x14;

  TileRenderer tileRenderer = new TileRenderer();

  public DT1 readDt1(String fileName, ByteInput in) {
    DT1 dt1 = new DT1();
    dt1.fileName = fileName;
    // try {
    //   MDC.put("dt1", dt1.fileName);
    //   log.trace("Reading dt1...");
    //   log.trace("Validating dt1 signature");
      in.readSignature(SIGNATURE);
      readHeader(in, dt1);
      readTiles(in, dt1);
      readBlocks(in, dt1);
      if (DT1.loadData) {
        for (int i = 0, s = dt1.numTiles; i < s; i++) {
          Tile tile = dt1.tiles[i];
          tile.pixmap = tileRenderer.render2(tile);
        }
      }
      return dt1;
    // } finally {
    //   MDC.remove("dt1");
    // }
  }


  DT1 readHeader(ByteInput in, DT1 dt1) {
    in = in.readSlice(HEADER_SIZE - SIGNATURE.length);
    in.skipBytes(RESERVED);
    dt1.numTiles = in.readSafe32u();
    dt1.tileOffset = in.readSafe32u();
    if (dt1.tileOffset != HEADER_SIZE) {
      throw new InvalidFormat(
          in,
          String.format("Tile offset (0x%x) does not match expected header size (0x%x)",
              dt1.tileOffset, HEADER_SIZE));
    }
    assert in.bytesRemaining() == 0 : "in.bytesRemaining(" + in.bytesRemaining() + ") > " + 0;
    return dt1;
  }

  DT1 readTiles(ByteInput in, DT1 dt1) {
    int numTiles = dt1.numTiles;
    Tile[] tiles = dt1.tiles = new Tile[numTiles];
    for (int i = 0, s = numTiles; i < s; i++) {
      // try {
      //   MDC.put("tile", i);
        tiles[i] = readTile(in, dt1);
      // } finally {
      //   MDC.remove("tile");
      // }
    }
    return dt1;
  }

  Tile readTile(ByteInput in, DT1 dt1) {
    in = in.readSlice(TILE_HEADER_SIZE);
    Tile tile = new Tile();
    tile.direction = in.readSafe32u();
    // log.trace("tile.direction: {} ({})", tile.direction, Orientation.directionToString(tile.direction));
    tile.roofHeight = in.read16u();
    // log.trace("tile.roofHeight: {}", tile.roofHeight);
    tile.soundIndex = in.read8u();
    // log.trace("tile.soundIndex: {}", tile.soundIndex);
    tile.animated = in.read8();
    // log.trace("tile.animated: {}", tile.animated);
    tile.height = in.read32();
    // log.trace("tile.height: {}", tile.height);
    tile.width = in.read32();
    // log.trace("tile.width: {}", tile.width);
    in.skipBytes(4); // unknown -- zeroes
    tile.orientation = in.readSafe32u();
    // log.trace("tile.orientation: {} ({})", tile.orientation, Orientation.toString(tile.orientation));
    tile.mainIndex = in.readSafe32u();
    // log.trace("tile.mainIndex: {}", tile.mainIndex);
    tile.subIndex = in.readSafe32u();
    // log.trace("tile.subIndex: {}", tile.subIndex);
    int tileIndex = tile.updateIndex();
    // log.tracef("tile.tileIndex: 0x%08x", tileIndex);
    tile.rarity = in.readSafe32u();
    // log.trace("tile.rarity: {}", tile.rarity);
    tile.unknown = in.read32();
    // log.tracef("tile.unknown: %1$d (0x%1$08x)", tile.unknown);
    tile.flags = in.readBytes(Tile.NUM_SUBTILES);
    // if (log.traceEnabled()) log.trace("tile.flags: {}", DebugUtils.toByteArray(tile.flags));
    in.skipBytes(7); // DT1.NUM_SUBTILES + 7 = 32 bytes to align it better?
    tile.blocksOffset = in.readSafe32u();
    // log.tracef("tile.blocksOffset: +0x%08X", tile.blocksOffset);
    tile.blocksLength = in.readSafe32u();
    // log.tracef("tile.blocksLength: +0x%08X", tile.blocksLength);
    tile.numBlocks = in.readSafe32u();
    // log.trace("tile.numBlocks: {}", tile.numBlocks);
    tile.blocks = tile.numBlocks == 0 ? Block.EMPTY_BLOCK_ARRAY : new Block[tile.numBlocks];
    tile.box = new BBox().setZero();
    if (DEBUG_ZEROS) {
      // 12 bytes -> 4 zeroes, 4 bytes, 4 zeroes
      in.skipBytes(4); // all zeroes
      byte[] unknownBytes = in.readBytes(4);
      tile.unknown2 = Conversion.byteArrayToInt(unknownBytes, 0, 0, 0, unknownBytes.length);
      if (!ArrayUtils.allZeroes(unknownBytes)) {
        // All tiles in dt1 should match non-zero bytes, log non-sequential variations though
        if (!Arrays.equals(unknownBytes, dt1.nonZeroBytes)) {
          dt1.nonZeroBytes = unknownBytes;
          log.warnf("Block data contains non-zero bytes +%08x: %s",
              in.mark(), DebugUtils.toByteArray(unknownBytes));
        }
      }
      in.skipBytes(4); // all zeroes
    } else {
      in.skipBytes(4); // all zeroes
      tile.unknown2 = in.read32();
      in.skipBytes(4); // all zeroes
    }
    assert in.bytesRemaining() == 0 : "in.bytesRemaining(" + in.bytesRemaining() + ") > " + 0;
    return tile;
  }

  DT1 readBlocks(ByteInput in, DT1 dt1) {
    for (int i = 0, s = dt1.numTiles; i < s; i++) {
      // try {
      //   MDC.put("tile", i);
        Tile tile = dt1.tiles[i];
        readBlocks(in, dt1, tile);
      // } finally {
      //   MDC.remove("tile");
      // }
    }
    return dt1;
  }

  void readBlocks(ByteInput in, DT1 dt1, Tile tile) {
    BBox box = tile.box.prepare();
    Block[] blocks = tile.blocks;
    for (int i = 0, s = tile.numBlocks; i < s; i++) {
      // try {
      //   MDC.put("block", i);
        Block block = blocks[i] = readBlock(in, dt1, tile);
        updateBounds(box, block);
      // } finally {
      //   MDC.remove("block");
      // }
    }
    box.update();

    for (int i = 0, s = tile.numBlocks; i < s; i++) {
      // try {
      //   MDC.put("block", i);
        Block block = blocks[i];
        block.data = in.readBytes(block.size);
      // } finally {
      //   MDC.remove("block");
      // }
    }
  }

  Block readBlock(ByteInput in, DT1 dt1, Tile tile) {
    in = in.readSlice(BLOCK_SIZE);
    Block block = new Block();
    block.x = in.read16();
    // log.trace("block.x: {}", block.x);
    block.y = in.read16();
    // log.trace("block.y: {}", block.y);
    in.skipBytes(2); // unknown - zeros
    block.gridX = in.read8u();
    // log.trace("block.gridX: {}", block.gridX);
    block.gridY = in.read8u();
    // log.trace("block.gridY: {}", block.gridY);
    block.format = in.read16();
    // log.tracef("block.format: 0x%04x", block.format);
    if (!Block.knownFormat(block.format)) log.warnf("Unknown block format: 0x%04x", block.format);
    block.size = in.readSafe32u();
    // log.tracef("block.size: 0x%08x", block.size);
    in.skipBytes(2); // unknown - zeros
    block.dataOffset = in.readSafe32u();
    // log.tracef("block.dataOffset: +0x%08x", block.dataOffset);
    assert in.bytesRemaining() == 0 : "in.bytesRemaining(" + in.bytesRemaining() + ") > " + 0;
    return block;
  }

  void updateBounds(BBox box, Block block) {
    final int x;
    final int y;
    switch (block.format) {
      case ISO_FORMAT:
      case ISO_RLE_FORMAT:
        // final int gridX = block.gridX;
        // final int gridY = block.gridY;
        x = block.x;
        y = block.y - Tile.HEIGHT;
        if (x < box.xMin) box.xMin = x;
        if (y < box.yMin) box.yMin = y;
        if (x + Tile.SUBTILE_WIDTH > box.xMax) box.xMax = x + Tile.SUBTILE_WIDTH;
        if (y + Tile.SUBTILE_HEIGHT > box.yMax) box.yMax = y + Tile.SUBTILE_HEIGHT;
        break;
      case RLE_FORMAT:
      default:
        x = block.x;
        y = block.y;
        if (x < box.xMin) box.xMin = x;
        if (y < box.yMin) box.yMin = y;
        if (x + Block.SIZE > box.xMax) box.xMax = x + Block.SIZE;
        if (y + Block.SIZE > box.yMax) box.yMax = y + Block.SIZE;
        break;
    }
  }
}
