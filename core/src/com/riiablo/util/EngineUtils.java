package com.riiablo.util;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.DT1;
import com.riiablo.map.DT1.Tile;

public class EngineUtils {
  private EngineUtils() {}

  public static Vector2 worldToScreenCoords(Vector2 world, Vector2 dst) {
    assert world != dst;
    dst.x = +(world.x * Tile.SUBTILE_WIDTH50)  - (world.y * DT1.Tile.SUBTILE_WIDTH50);
    dst.y = -(world.x * Tile.SUBTILE_HEIGHT50) - (world.y * Tile.SUBTILE_HEIGHT50);
    return dst;
  }

  public static Vector2 worldToScreenCoords(float x, float y, Vector2 dst) {
    dst.x = +(x * Tile.SUBTILE_WIDTH50)  - (y * DT1.Tile.SUBTILE_WIDTH50);
    dst.y = -(x * Tile.SUBTILE_HEIGHT50) - (y * Tile.SUBTILE_HEIGHT50);
    return dst;
  }

  public static GridPoint2 worldToScreenCoords(GridPoint2 world, GridPoint2 dst) {
    assert world != dst;
    dst.x = +(world.x * Tile.SUBTILE_WIDTH50)  - (world.y * Tile.SUBTILE_WIDTH50);
    dst.y = -(world.x * Tile.SUBTILE_HEIGHT50) - (world.y * Tile.SUBTILE_HEIGHT50);
    return dst;
  }

  public static GridPoint2 worldToScreenCoords(int x, int y, GridPoint2 dst) {
    dst.x = +(x * Tile.SUBTILE_WIDTH50)  - (y * Tile.SUBTILE_WIDTH50);
    dst.y = -(x * Tile.SUBTILE_HEIGHT50) - (y * Tile.SUBTILE_HEIGHT50);
    return dst;
  }
}
