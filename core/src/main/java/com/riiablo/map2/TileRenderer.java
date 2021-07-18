package com.riiablo.map2;

import com.badlogic.gdx.graphics.Pixmap;

import com.riiablo.codec.Palette;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedPixmap;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.DT1.Tile.Block;

import static com.riiablo.map2.DT1.Tile.Block.ISOMETRIC_SIZE;
import static com.riiablo.map2.DT1.Tile.Block.ISO_FORMAT;
import static com.riiablo.map2.DT1.Tile.Block.ISO_RLE_FORMAT;
import static com.riiablo.map2.DT1.Tile.Block.RLE_FORMAT;
import static com.riiablo.map2.Orientation.FLOOR;
import static com.riiablo.map2.Orientation.ROOF;

public class TileRenderer {
  private static final Logger log = LogManager.getLogger(TileRenderer.class);

  Pixmap render(Tile tile) {
    int width = tile.width;
    int height = -tile.height; // invert y-axis

    int orientation = tile.orientation;
    if (orientation == FLOOR || orientation == ROOF) {
      if (height != 0) {
        height = Tile.HEIGHT; // encoded as 128px, set to Tile.HEIGHT
      }
    } else if (orientation < ROOF) {
      if (height != 0) {
        height -= Block.SIZE; // trim topmost blocks (always empty)
      }
    } else {

    }

    Pixmap pixmap = new PaletteIndexedPixmap(width, height);

    Block[] blocks = tile.blocks;
    for (int i = 0, s = tile.numBlocks; i < s; i++) {
      drawBlock(pixmap, blocks[i], tile.box, height);
    }

    return pixmap;
  }

  Pixmap render2(Tile tile) {
    BBox box = tile.box;
    Pixmap pixmap = new PaletteIndexedPixmap(box.width, box.height);

    Block[] blocks = tile.blocks;
    for (int i = 0, s = tile.numBlocks; i < s; i++) {
      drawBlock2(pixmap, blocks[i], box);
    }

    return pixmap;
  }

  void drawBlock2(Pixmap pixmap, Block block, BBox box) {
    switch (block.format) {
      case ISO_FORMAT:
        // iso x,y relative to top-left w/ y-down and tile height offset pre-applied
        drawIsometric(pixmap, block,
            block.x - box.xMin,
            block.y - Tile.HEIGHT - box.yMin);
        break;
      case ISO_RLE_FORMAT:
        // iso x,y relative to top-left w/ y-down and tile height offset pre-applied
        drawRLE(pixmap, block,
            block.x - box.xMin,
            block.y - Tile.HEIGHT - box.yMin);
        break;
      case RLE_FORMAT:
      default:
        // rle x,y are relative to top-left /w y-down
        drawRLE(pixmap, block, block.x - box.xMin, block.y - box.yMin);
        break;
    }
  }

  void drawBlock(Pixmap pixmap, Block block, BBox box, int yOffset) {
    switch (block.format) {
      case ISO_FORMAT:
        // yOffset not needed because x,y are conveniently correct for OpenGL texture coords
        drawIsometric(pixmap, block, block.x, block.y);
        break;
      case ISO_RLE_FORMAT:
        // yOffset not needed because x,y are conveniently correct for OpenGL texture coords
        drawRLE(pixmap, block, block.x, block.y);
        break;
      case RLE_FORMAT:
      default:
        // rle x,y are relative to top-left
        drawRLE(pixmap, block, block.x, block.y + yOffset);
        break;
    }
  }

  void drawIsometric(Pixmap pixmap, Block block, int x0, int y0) {
    final int format = block.format;
    if (format != ISO_FORMAT) {
      throw new UnsupportedOperationException(
          "block.format(" + format + ") != ISOMETRIC_FORMAT(" + ISO_FORMAT + ")");
    }

    if (block.size != ISOMETRIC_SIZE) {
      throw new UnsupportedOperationException(
          "block.size(" + block.size + ") != ISOMETRIC_SIZE(" + ISOMETRIC_SIZE + ")");
    }

    final int[] ISO_X_LEN = Block.ISO_X_LEN;
    final int[] ISO_X_OFF = Block.ISO_X_OFF;

    /**
     * x0 = pixmap x-offset (constant)
     * y0 = pixmap y-offset (incremented per run)
     * x = pixmap x-offset + block x-offset (assigned per run)
     * y = pixmap y-offset (incremented per run)
     */
    int x, y = 0, run, d = 0;
    final byte[] data = block.data;
    for (int i = 0, s = ISOMETRIC_SIZE; i < s; i += run, y++, y0++) {
      run = ISO_X_LEN[y];
      x = x0 + ISO_X_OFF[y];
      for (int r = 0; r < run; r++) {
        pixmap.drawPixel(x++, y0, Palette.a8(data[d++]));
      }
    }
  }

  void drawRLE(Pixmap pixmap, Block block, int x0, int y0) {
    int d = 0;
    int b1, b2;
    int x = x0, y = y0;
    final byte[] data = block.data;
    for (int i = 0, s = block.size; i < s; ) {
      b1 = data[d++] & 0xFF;
      b2 = data[d++] & 0xFF;
      i += 2;
      if (b1 > 0 || b2 > 0) {
        x += b1;
        i += b2;
        for (; b2 > 0; b2--) {
          pixmap.drawPixel(x++, y, Palette.a8(data[d++]));
        }
      } else {
        x = x0;
        y++;
      }
    }
  }
}
