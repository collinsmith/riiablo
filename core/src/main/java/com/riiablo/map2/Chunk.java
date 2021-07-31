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
import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;

/** acts as a dt1 Tile cache chunk of zone */
public class Chunk extends BBox implements Poolable, Disposable {
  static final Pool<Chunk> pool = Pools.get(Chunk.class);

  public static Chunk obtain(int x, int y, int width, int height) {
    Chunk chunk = pool.obtain();
    chunk.asBox(x, y, width, height);
    chunk.numTiles = width * height / SUBTILE_SIZE;
    chunk.tiles = tilePools.obtain(chunk.numTiles);
    return chunk;
  }

  @Override
  public void reset() {
  }

  @Override
  public void dispose() {
    numTiles = 0;
    tilePools.free(tiles);
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
  public int numTiles;
  public Tile[] tiles;

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
        drawDebugSubtiles(pixmap, x, y);
        break;
    }
  }

  void drawDebugSubtiles(Pixmap pixmap, int x, int y) {
    Tile[] tiles = this.tiles;
    for (int i = 0, s = numTiles; i < s; i++) {
      Tile tile = tiles[i];
      if (tile == null) continue;
      drawDebugSubtile(tile, pixmap, x, y);
    }
  }

  void drawDebugSubtile(Tile tile, Pixmap pixmap, int x, int y) {
    // TODO: apply tile offset + subtile offset of flags id
    byte[] flags = tile.flags;
    for (int i = 0; i < Tile.NUM_SUBTILES; i++) {
      byte flag = flags[i];
      if (flag == 0) continue;
      pixmap.drawPixel(x + i % 25, y + i / 25);
    }
  }
}
