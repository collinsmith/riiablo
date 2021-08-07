package com.riiablo.map2;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.codec.util.BBox;
import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.util.BucketPool;
import com.riiablo.map2.util.DebugMode;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.riiablo.map2.DS1.MAX_LAYERS;
import static com.riiablo.map2.DT1.Tile.NUM_SUBTILES;
import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;

/** acts as a dt1 Tile cache chunk of zone */
public class Chunk extends BBox implements Poolable, Disposable {
  static final Pool<Chunk> pool = Pools.get(Chunk.class);

  static final BucketPool<Tile[]> tilePools = BucketPool
      .builder(Tile[].class)
      .add(8 * 8)
      .add(16 * 16)
      .add(24 * 24)
      .add(32 * 32)
      .add(64 * 64)
      .build();

  static final BucketPool<byte[]> bytePools = BucketPool
      .builder(byte[].class)
      .add(8 * 8)
      .add(16 * 16)
      .add(24 * 24)
      .add(32 * 32)
      .add(64 * 64)
      .scl(NUM_SUBTILES)
      .build();

  public int layers;
  public int numTiles;
  public final Tile[][] tiles = new Tile[MAX_LAYERS][];
  public byte[] flags;

  public static Chunk obtain(int x, int y, int width, int height) {
    Chunk chunk = pool.obtain();
    chunk.asBox(x, y, width, height);
    chunk.layers = 0;
    chunk.numTiles = width * height / NUM_SUBTILES;
    chunk.flags = bytePools.obtain(width * height);
    return chunk;
  }

  @Override
  public void reset() {
    layers = 0;
    numTiles = 0;
    for (int i = 0, s = tiles.length; i < s; i++) {
      Tile[] tiles = this.tiles[i];
      if (tiles == null) continue;
      tilePools.free(tiles);
      Arrays.fill(tiles, 0, numTiles, null);
      this.tiles[i] = null;
    }
    bytePools.free(flags);
    flags = null;
  }

  @Override
  public void dispose() {
    pool.free(this);
  }

  Chunk init(int layers) {
    if (this.layers != 0) throw new IllegalStateException("chunk already initialized");
    this.layers = layers;
    for (int i = 0; i < MAX_LAYERS; i++) {
      if ((layers & (1 << i)) != 0) {
        tiles[i] = tilePools.obtain(numTiles);
      }
    }

    return this;
  }

  public Chunk updateFlags() {
    for (int i = 0; i < MAX_LAYERS; i++) {
      if ((layers & (1 << i)) == 0) continue;
      Tile[] tiles = this.tiles[i];
      for (int y = 0, j = 0; y <= height; y += SUBTILE_SIZE) {
        for (int x = 0; x <= width; x += SUBTILE_SIZE) {
          mergeFlags(flags, width, tiles[j++].flags, x, y);
        }
      }

      // TODO:
      // update only takes into account chunk tiles, but needs to account for
      // ds1 tile overrides.
    }

    return this;
  }

  static void mergeFlags(byte[] dst, int width, byte[] flags, int x, int y) {
    int dXY, i = 0;
    for (int dy = 0; dy < SUBTILE_SIZE; dy++) {
      // flags are stored in reversed row order: 20..24,15..19,10..14,5..9,0..4
      dXY = (y + SUBTILE_SIZE - 1 - dy) * width + x;
      for (int dx = 0; dx < SUBTILE_SIZE; dx++) {
        dst[dXY++] |= flags[i++];
      }
    }
  }

  Tile[] tiles(int layer) {
    Tile[] tiles = this.tiles[layer];
    if (tiles == null) throw new IllegalArgumentException("layer(" + layer + ") does not exist!");
    return tiles;
  }

  int color = MathUtils.random.nextInt() | 0xff;

  void drawDebug(DebugMode mode, Pixmap pixmap, int x, int y) {
    switch (mode) {
      case CHUNK:
        pixmap.setColor(color);
        pixmap.drawRectangle(
                x + xMin,
                y + yMin,
                width,
                height);
        break;
      case SUBTILE:
        pixmap.setColor(RED);
        drawDebugSubtiles(pixmap, x + xMin, y + yMin);
        break;
    }
  }

  void drawDebugSubtiles(Pixmap pixmap, int x, int y) {
    for (int i = 0, s = width * height; i < s; i++) {
      if (flags[i] == 0) continue;
      pixmap.drawPixel(
          x + (i % width),
          y + (i / width));
    }
  }
}
