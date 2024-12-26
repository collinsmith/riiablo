package com.riiablo.map5;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.asset.AssetUtils;

import static com.riiablo.map5.Dt1.MISSING_TEXTURE;

public class Tile implements Disposable {
  public static final int SUBTILE_SIZE = 5;
  public static final int NUM_SUBTILES = SUBTILE_SIZE * SUBTILE_SIZE;

  public static final int WIDTH = 160;
  public static final int HEIGHT = 80;
  public static final int WIDTH50 = WIDTH / 2;
  public static final int HEIGHT50 = HEIGHT / 2;

  public static final int SUBTILE_WIDTH = WIDTH / SUBTILE_SIZE;
  public static final int SUBTILE_HEIGHT = HEIGHT / SUBTILE_SIZE;
  public static final int SUBTILE_WIDTH50 = SUBTILE_WIDTH / 2;
  public static final int SUBTILE_HEIGHT50 = SUBTILE_HEIGHT / 2;

  public static final int WALL_HEIGHT = 96;

  static final Pool<Tile> pool = Pools.get(Tile.class, Integer.MAX_VALUE);

  static Tile obtain() {
    return pool.obtain();
  }

  @Override
  public void dispose() {
    pool.free(this);
    AssetUtils.disposeQuietly(pixmap);
    pixmap = null;
    AssetUtils.disposeQuietly(texture);
    texture = null;
    region.setTexture(MISSING_TEXTURE);
  }

  public static final int MATERIAL_OTHER    = 0x0001;
  public static final int MATERIAL_WATER    = 0x0002;
  public static final int MATERIAL_WOOD_OBJ = 0x0004; // block reverb
  public static final int MATERIAL_ISTONE   = 0x0008;
  public static final int MATERIAL_OSTONE   = 0x0010;
  public static final int MATERIAL_DIRT     = 0x0020;
  public static final int MATERIAL_SAND     = 0x0040;
  public static final int MATERIAL_WOOD     = 0x0080;
  public static final int MATERIAL_LAVA     = 0x0100; // lighting + animated
  public static final int MATERIAL_SNOW     = 0x0400;
  public static final int ANIMATED_MATERIAL = MATERIAL_LAVA;

  public int lightDirection;
  public int roofHeight;
  public int materialFlags;
  public int height;
  public int width;
  public int heightToBottom;
  public int orientation;
  public int mainIndex;
  public int subIndex;
  public int rarityFrame;
  public byte[] flags = new byte[NUM_SUBTILES]; // 25+7
  public int blocksOffset;
  public int blocksLength;
  public int numBlocks;
  public short cacheIndex;

  int tileIndex; // synthetic
  Pixmap pixmap;
  Texture texture;
  final TextureRegion region = new TextureRegion(MISSING_TEXTURE);

  int updateIndex() {
    return tileIndex = Index.create(orientation, mainIndex, subIndex);
  }

  public int tileIndex() {
    return tileIndex;
  }

  void uploadTexture() {
    texture = new Texture(pixmap);
    region.setRegion(texture);
    pixmap.dispose();
    pixmap = null;
  }

  public boolean animated() {
    return (materialFlags & ANIMATED_MATERIAL) != 0;
  }

  public TextureRegion texture() {
    return region;
  }

  public int textureWidth() {
    if (texture != null) {
      return texture.getWidth();
    } else if (pixmap != null) {
      return pixmap.getWidth();
    } else {
      return -1;
    }
  }

  public int textureHeight() {
    if (texture != null) {
      return texture.getHeight();
    } else if (pixmap != null) {
      return pixmap.getHeight();
    } else {
      return -1;
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("tileIndex", String.format("0x%08x", tileIndex))
        .append("orientation", String.format("%d (%s)", orientation, Orientation.toString(orientation)))
        .append("mainIndex", mainIndex)
        .append("subIndex", subIndex)
        .append("lightDirection", String.format("%d (%s)", lightDirection, Orientation.directionToString(lightDirection)))
        .append("roofHeight", roofHeight)
        .append("materialFlags", materialFlags)
        .append("height", height)
        .append("width", width)
        .append("heightToBottom", heightToBottom)
        .append("rarityFrame", rarityFrame)
        .append("flags", flags)
        .append("blocksOffset", String.format("+0x%x", blocksOffset))
        .append("blocksLength", String.format("0x%x", blocksLength))
        .append("numBlocks", numBlocks)
        .append("cacheIndex", cacheIndex)
        // .append("blocks", blocks)
        .toString();
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
}
