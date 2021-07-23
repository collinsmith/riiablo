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

/** acts as a dt1 Tile cache chunk of zone */
public class Chunk extends BBox implements Poolable, Disposable {
  static final Pool<Chunk> pool = Pools.get(Chunk.class);

  public static Chunk obtain(int x, int y, int width, int height) {
    Chunk chunk = pool.obtain();
    chunk.asBox(x, y, width, height);
    // chunk.tiles = tilePools.obtain(chunk.width * chunk.height);
    return chunk;
  }

  @Override
  public void reset() {
  }

  @Override
  public void dispose() {
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
  public Tile[] tiles;

  int color = MathUtils.random.nextInt() | 0xff;

  void drawDebug(Pixmap pixmap, int x, int y) {
    pixmap.setColor(color);
    pixmap.drawRectangle(
        x + xMin,
        y + yMin,
        width,
        height);
  }
}
