package com.riiablo.map2;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.codec.util.BBox;
import com.riiablo.map2.util.DebugMode;

import static com.riiablo.map2.DT1.Tile.SUBTILE_SIZE;

public final class Zone extends BBox implements Poolable, Disposable {
  public static final Pool<Zone> pool = Pools.get(Zone.class, 16);
  static final Pool<Chunk> chunkPool = Chunk.pool;

  public static Zone obtain(int x, int y, int width, int height, int chunkWidth, int chunkHeight) {
    assert (width / SUBTILE_SIZE) % chunkWidth == 0
        : "width(" + width + ") / SUBTILE_SIZE(" + SUBTILE_SIZE + ") is not evenly divisible by chunkWidth(" + chunkWidth + ")";
    assert (height / SUBTILE_SIZE) % chunkHeight == 0
        : "height(" + height + ") / SUBTILE_SIZE(" + SUBTILE_SIZE + ") is not evenly divisible by chunkHeight(" + chunkHeight + ")";
    Zone zone = pool.obtain();
    zone.asBox(x, y, width, height);
    zone.chunksX = width / SUBTILE_SIZE / chunkWidth;
    zone.chunksY = height / SUBTILE_SIZE / chunkHeight;
    zone.chunkWidth = chunkWidth;
    zone.chunkHeight = chunkHeight;
    obtainChunks(
        zone.xMin, zone.yMin,
        zone.chunks,
        zone.chunksX, zone.chunksY,
        zone.chunkWidth * SUBTILE_SIZE, zone.chunkHeight * SUBTILE_SIZE);
    return zone;
  }

  /**
   * @param zoneX,zoneY in subtiles
   * @param chunksX,chunksY in tiles
   * @param chunkWidth,chunkHeight in subtiles
   */
  static void obtainChunks(
      int zoneX, int zoneY,
      Array<Chunk> chunks,
      int chunksX, int chunksY,
      int chunkWidth, int chunkHeight
  ) {
    for (int y = 0, chunkY = 0; y < chunksY; y++, chunkY += chunkHeight) {
      for (int x = 0, chunkX = 0; x < chunksX; x++, chunkX += chunkWidth) {
        Chunk chunk = Chunk.obtain(
            zoneX + chunkX,
            zoneY + chunkY,
            chunkWidth,
            chunkHeight);
        chunks.add(chunk);
      }
    }
  }

  @Override
  public void reset() {
    chunkPool.freeAll(chunks);
  }

  @Override
  public void dispose() {
    pool.free(this);
  }

  public int chunkHeight;
  public int chunkWidth;
  public int chunksX;
  public int chunksY;
  public final Array<Chunk> chunks = new Array<>(256); // TODO: ChunkGrid?

  public Chunk get(int x, int y) {
    return chunks.get(y * chunkWidth + x);
  }

  int color = MathUtils.random.nextInt() | 0xff;

  public void drawDebug(DebugMode mode, Pixmap pixmap, int x, int y) {
    switch (mode) {
      case CHUNK:
        drawDebugChunk(pixmap, x, y);
        break;
      case PREFAB:
        drawDebugPrefab(pixmap, x, y);
        break;
      case TILE:
        drawDebugTile(pixmap, x, y);
        break;
      case SUBTILE:
        drawDebugSubtile(pixmap, x, y);
        break;
      default:
    }
  }

  void drawDebugChunk(Pixmap pixmap, int x, int y) {
    for (Chunk chunk : chunks) chunk.drawDebug(pixmap, x, y);
  }

  void drawDebugPrefab(Pixmap pixmap, int x, int y) {
    // TODO: implement
  }

  void drawDebugTile(Pixmap pixmap, int x, int y) {
    pixmap.setColor(color);
    pixmap.drawRectangle(
        x + xMin / SUBTILE_SIZE,
        y + yMin / SUBTILE_SIZE,
        width / SUBTILE_SIZE,
        height / SUBTILE_SIZE);
  }

  void drawDebugSubtile(Pixmap pixmap, int x, int y) {
    pixmap.setColor(color);
    pixmap.drawRectangle(x + xMin, y + yMin, width, height);
  }
}
