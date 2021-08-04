package com.riiablo.map2;

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

  public static Chunk obtain(int x, int y, int width, int height) {
    Chunk chunk = pool.obtain();
    chunk.asBox(x, y, width, height);
    chunk.layers = 0;
    chunk.numTiles = width * height / SUBTILE_SIZE;
    return chunk;
  }

  @Override
  public void reset() {
  }

  @Override
  public void dispose() {
    layers = 0;
    numTiles = 0;
    for (int i = 0, s = tiles.length; i < s; i++) {
      if (tiles[i] != null) {
        tilePools.free(tiles[i]);
        tiles[i] = null;
      }
    }
    pool.free(this);
  }

  static final BucketPool<Tile[]> tilePools = BucketPool
      .builder(Tile[].class)
      .add(8 * 8)
      .add(16 * 16)
      .add(24 * 24)
      .add(32 * 32)
      .add(64 * 64)
      .build();

  // public Zone zone;
  public int layers;
  public int numTiles;
  public final Tile[][] tiles = new Tile[MAX_LAYERS][];

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
    int chunkWidth = width / SUBTILE_SIZE;
    Tile[] tiles = this.tiles[4];
    if (tiles == null) return; // TODO: remove when above is corrected
    for (int i = 0, s = numTiles; i < s; i++) {
      Tile tile = tiles[i];
      if (tile == null) continue;
      drawDebugSubtile(tile, pixmap,
          x + (i % chunkWidth) * SUBTILE_SIZE,
          y + (i / chunkWidth) * SUBTILE_SIZE);
    }
  }

  void drawDebugSubtile(Tile tile, Pixmap pixmap, int x, int y) {
    // pixmap.drawPixel(x, y);
    // TODO: apply tile offset + subtile offset of flags id
    byte[] flags = tile.flags;
    for (int i = 0; i < NUM_SUBTILES; i++) {
      byte flag = flags[i];
      if (flag == 0) continue;
      pixmap.drawPixel(x + (i % SUBTILE_SIZE), y + (i / SUBTILE_SIZE));
    }
  }
}
