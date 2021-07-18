package com.riiablo.map2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.codec.util.BBox;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.DebugUtils;

public class DT1 implements Disposable {
  private static final Logger log = LogManager.getLogger(DT1.class);

  // Disables loading GL texture data in headless environments
  public static boolean loadData = true;

  String fileName;
  int tileOffset;

  byte[] nonZeroBytes;
  int numTiles;
  Tile[] tiles;
  Texture[] textures;

  public int numTiles() {
    return numTiles;
  }

  public Tile tile(int i) {
    return tiles[i];
  }

  public TextureRegion texture(int i) {
    return tiles[i];
  }

  @Override
  public void dispose() {
    if (!loadData) return;
    Texture[] textures = this.textures;
    for (int i = 0, s = numTiles; i < s; i++) {
      textures[i].dispose();
      textures[i] = null;
    }
  }

  public void prepareTextures() {
    if (!loadData) return;
    if (textures != null) {
      throw new IllegalStateException(
          "textures have already been prepared");
    }

    textures = new Texture[tiles.length];
    for (int i = 0, s = numTiles; i < s; i++) {
      Texture texture = new Texture(new PixmapTextureData(tiles[i].pixmap, null, false, false, false));
      //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
      texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
      tiles[i].setRegion(textures[i] = texture);
    }
  }

  public static class Tile extends TextureRegion {
    public static final int WIDTH = 160;
    public static final int HEIGHT = 80;
    public static final int WIDTH50 = WIDTH / 2;
    public static final int HEIGHT50 = HEIGHT / 2;

    public static final int SUBTILE_SIZE = 5;
    public static final int NUM_SUBTILES = SUBTILE_SIZE * SUBTILE_SIZE;

    public static final int SUBTILE_WIDTH = WIDTH / SUBTILE_SIZE;
    public static final int SUBTILE_HEIGHT = HEIGHT / SUBTILE_SIZE;
    public static final int SUBTILE_WIDTH50 = SUBTILE_WIDTH / 2;
    public static final int SUBTILE_HEIGHT50 = SUBTILE_HEIGHT / 2;

    public int direction;
    public int roofHeight;
    public int soundIndex;
    public int animated;
    public int width;
    public int height;
    public int orientation;
    public int mainIndex;
    public int subIndex;
    public int rarity; // frame index if animated
    public int unknown;
    public int unknown2;
    public byte[] flags;

    int numBlocks;
    Block[] blocks;
    int blocksOffset;
    int blocksLength;

    public int tileIndex;
    Pixmap pixmap;
    BBox box; // coords are y-down, min top-left, max bottom-right

    int updateIndex() {
      return tileIndex = Index.create(orientation, mainIndex, subIndex);
    }

    public boolean isFloor() {
      return Orientation.isFloor(orientation);
    }

    public boolean isWall() {
      return Orientation.isWall(orientation);
    }

    public boolean isRoof() {
      return Orientation.isRoof(orientation);
    }

    public boolean isSpecial() {
      return Orientation.isSpecial(orientation);
    }

    public boolean isLowerWall() {
      return Orientation.isLowerWall(orientation);
    }

    public void draw(Batch batch, float x, float y) {
      batch.draw(this, x + box.xMin, y - box.yMax);
    }

    public void drawDebug(ShapeRenderer shapes, float x, float y) {
      shapes.set(ShapeRenderer.ShapeType.Line);
      shapes.setColor(Color.DARK_GRAY);
      final int maxY = -box.yMin;
      final int minY = box.yMax;
      for (int yOffs = 0; yOffs <= maxY; yOffs += Block.SIZE) {
        shapes.line(x, y + yOffs, x + Tile.WIDTH, y + yOffs);
      }
      for (int yOffs = Block.SIZE; yOffs <= minY; yOffs += Block.SIZE) {
        shapes.line(x, y - yOffs, x + Tile.WIDTH, y - yOffs);
      }
      for (int xOffs = 0; xOffs <= Tile.WIDTH; xOffs += Block.SIZE) {
        shapes.line(x + xOffs, y, x + xOffs, y + maxY);
      }
      for (int xOffs = 0; xOffs <= Tile.WIDTH; xOffs += Block.SIZE) {
        shapes.line(x + xOffs, y - minY, x + xOffs, y);
      }

      shapes.setColor(Color.LIGHT_GRAY);
      for (int i = 0, s = numBlocks; i < s; i++) {
        blocks[i].drawDebug(shapes, x, y);
      }

      shapes.setColor(Color.WHITE);
      DebugUtils.drawDiamond2(shapes, x, y, Tile.WIDTH, Tile.HEIGHT);

      /**
       * green = self-reported tile size -1px padding
       * red = texture +1px padding
       * white = bbox
       */

      shapes.setColor(Color.GREEN);
      shapes.rect(x - 1, y - height + -box.yMax - 1, width + 2, height + 2);

      shapes.setColor(Color.RED);
      shapes.rect(x + box.xMin + 1, y - box.yMax + 1, getRegionWidth() - 2, getRegionHeight() - 2);

      shapes.setColor(Color.WHITE);
      shapes.rect(x + box.xMin, y - box.yMax, box.width, box.height);

      shapes.setColor(Color.MAGENTA);
      shapes.line(x - Tile.WIDTH, y, x + Tile.WIDTH + Tile.WIDTH, y);
      shapes.set(ShapeRenderer.ShapeType.Filled);
      shapes.rect(x - 3, y - 3, 6, 6);
    }

    public static final class Index {
      private Index() {}

      private static final int MAIN_INDEX_OFFSET  = 16;
      private static final int MAIN_INDEX_BITS    = 0xFF;

      private static final int SUB_INDEX_OFFSET   = 8;
      private static final int SUB_INDEX_BITS     = 0xFF;

      private static final int ORIENTATION_OFFSET = 0;
      private static final int ORIENTATION_BITS   = 0xFF;

      public static int create(int orientation, int mainIndex, int subIndex) {
        return (mainIndex   & MAIN_INDEX_BITS)  << MAIN_INDEX_OFFSET
             | (subIndex    & SUB_INDEX_BITS)   << SUB_INDEX_OFFSET
             | (orientation & ORIENTATION_BITS) << ORIENTATION_OFFSET;
      }

      public static int mainIndex(int index)   { return (index >>> MAIN_INDEX_OFFSET)  & MAIN_INDEX_BITS; }
      public static int subIndex(int index)    { return (index >>> SUB_INDEX_OFFSET)   & SUB_INDEX_BITS; }
      public static int orientation(int index) { return (index >>> ORIENTATION_OFFSET) & ORIENTATION_BITS; }
    }

    public static final class Block {
      static final Block[] EMPTY_BLOCK_ARRAY = new Block[0];

      static final int SIZE = 32;

      static final int ISO_FORMAT = 0x0001;
      static final int ISO_RLE_FORMAT = 0x2005;
      static final int RLE_FORMAT = 0x1001;
      static boolean knownFormat(short format) {
        switch (format) {
          case ISO_FORMAT:
          case ISO_RLE_FORMAT:
          case RLE_FORMAT:
            return true;
          default:
            return false;
        }
      }

      /**
       * Isometric blocks are 32x16, but since a square block only contains a
       * diamond's worth of pixels, that results in only needing to encode
       * exactly half of the area of the square, so 32x16 => 256 pixels.
       *
       * x-offset: boundary on left and right of run centering the iso pixels
       * x-len: number of isometric pixels encoded per run
       */
      static final int ISOMETRIC_SIZE = (SUBTILE_WIDTH * SUBTILE_HEIGHT) / 2;
      static final int ISO_X_OFF[] = {14, 12, 10, 8, 6, 4, 2, 0, 2, 4, 6, 8, 10, 12, 14, 16};
      static final int ISO_X_LEN[] = {4, 8, 12, 16, 20, 24, 28, 32, 28, 24, 20, 16, 12, 8, 4, 0};

      public int x;
      public int y;
      public int gridX; // iso = relative to NW subtile
      public int gridY; // iso = relative to NW subtile
      public short format;
      int size;
      int dataOffset;
      byte[] data;

      void drawDebug(ShapeRenderer shapes, float x0, float y0) {
        switch (format) {
          case ISO_FORMAT:
          case ISO_RLE_FORMAT:
            // iso x,y relative to top-left w/ y-down and tile height offset pre-applied
            DebugUtils.drawDiamondTL(shapes, x0 + x, y0 + Tile.HEIGHT - y, SUBTILE_WIDTH, SUBTILE_HEIGHT);
            break;
          case RLE_FORMAT:
          default:
            // rle x,y are relative to top-left
            shapes.rect(x0 + x, y0 - Block.SIZE - y, SIZE, SIZE);
            break;
        }
      }
    }
  }
}
