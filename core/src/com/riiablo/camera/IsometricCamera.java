package com.riiablo.camera;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.DT1.Tile;
import com.riiablo.util.EngineUtils;

public class IsometricCamera extends OrthographicCamera {

  private final Vector2 tmp = new Vector2();
  public final Vector2 offset = new Vector2();
  public final Vector2 position = new Vector2();

  public IsometricCamera() {}

  public void offset(float x, float y) {
    offset.set(x, y);
  }

  @Override
  public void translate(Vector2 vec) {
    position.add(vec);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp, 0);
  }

  @Override
  public void translate(float x, float y) {
    position.add(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp, 0).add(offset.x, offset.y, 0);
  }

  public void set(Vector2 vec) {
    set(vec.x, vec.y);
  }

  public void set(float x, float y) {
    position.set(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp.x, tmp.y, 0).add(offset.x, offset.y, 0);
  }

  public Vector2 toScreen(Vector2 worldCoords) {
    return toScreen(worldCoords.x, worldCoords.y, worldCoords);
  }

  public Vector2 toScreen(float x, float y, Vector2 dst) {
    return EngineUtils.worldToScreenCoords(x, y, dst);
  }

  public Vector2 toWorld(Vector2 screenCoords) {
    return toWorld(screenCoords.x, screenCoords.y, screenCoords);
  }

  public Vector2 toWorld(float x, float y, Vector2 dst) {
    x /= Tile.SUBTILE_WIDTH50;
    y /= Tile.SUBTILE_HEIGHT50;
    dst.x = ( x - y) / 2 - 0.5f;
    dst.y = (-x - y) / 2 - 0.5f;
    return dst;
  }

  public Vector2 toTile(Vector2 worldCoords) {
    return toTile(worldCoords.x, worldCoords.y, worldCoords);
  }

  public Vector2 toTile(float x, float y, Vector2 dst) {
    dst.x = x < 0 ? MathUtils.floor(x) : MathUtils.floorPositive(x);
    dst.y = y < 0 ? MathUtils.floor(y) : MathUtils.floorPositive(y);
    return dst;
  }

  public Vector2 toTile50(Vector2 worldCoords) {
    return toTile50(worldCoords.x, worldCoords.y, worldCoords);
  }

  public Vector2 toTile50(float x, float y, Vector2 dst) {
    dst.x = x < 0 ? MathUtils.round(x) : MathUtils.roundPositive(x);
    dst.y = y < 0 ? MathUtils.round(y) : MathUtils.roundPositive(y);
    return dst;
  }
}
