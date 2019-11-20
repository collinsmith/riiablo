package com.riiablo.camera;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.DT1.Tile;
import com.riiablo.util.EngineUtils;

public class IsometricCamera extends OrthographicCamera {

  private final Vector2 tmp = new Vector2();
  public final Vector2 position = new Vector2();

  private final Vector2 pixOffset  = new Vector2();
  private final Vector2 tileOffset = new Vector2();

  private final Builder builder = new Builder();

  public IsometricCamera() {}

  public void offset(float x, float y) {
    pixOffset.set(x, y);
    toWorld(pixOffset.x, pixOffset.y, tileOffset.setZero());
  }

  public Vector2 getTileOffset(Vector2 dst) {
    return dst.set(tileOffset);
  }

  @Override
  public void translate(Vector2 vec) {
    translate(vec.x, vec.y);
  }

  @Override
  public void translate(float x, float y) {
    position.add(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp, 0);
  }

  public void set(Vector2 vec) {
    set(vec.x, vec.y);
  }

  public void set(float x, float y) {
    position.set(x, y);
    toScreen(position.x, position.y, tmp);
    super.position.set(tmp, 0);
  }

  /**
   * Converts tile coords to screen coords.
   */
  public Vector2 toScreen(Vector2 worldCoords) {
    return toScreen(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Converts tile coords to screen coords.
   */
  public Vector2 toScreen(float x, float y, Vector2 dst) {
    EngineUtils.worldToScreenCoords(x, y, dst);
    return dst.add(pixOffset);
  }

  /**
   * Converts screen coords to tile coords.
   */
  public Vector2 toWorld(Vector2 screenCoords) {
    return toWorld(screenCoords.x, screenCoords.y, screenCoords);
  }

  /**
   * Converts screen coords to tile coords.
   */
  public Vector2 toWorld(float x, float y, Vector2 dst) {
    x /= Tile.SUBTILE_WIDTH50;
    y /= Tile.SUBTILE_HEIGHT50;
    dst.x = ( x - y) / 2 - tileOffset.x;
    dst.y = (-x - y) / 2 - tileOffset.y;
    return dst;
  }

  /**
   * Rounds tile coords to the floor integer tile coords from the specified float tile coords.
   */
  public Vector2 toTile(Vector2 worldCoords) {
    return toTile(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Rounds tile coords to the floor integer tile coords from the specified float tile coords.
   */
  public Vector2 toTile(float x, float y, Vector2 dst) {
    x += tileOffset.x;
    y += tileOffset.y;
    dst.x = x < 0 ? MathUtils.floor(x) : MathUtils.floorPositive(x);
    dst.y = y < 0 ? MathUtils.floor(y) : MathUtils.floorPositive(y);
    return dst;
  }

  /**
   * Rounds tile coords to the closest integer tile coords from the specified float tile coords.
   */
  public Vector2 toTile50(Vector2 worldCoords) {
    return toTile50(worldCoords.x, worldCoords.y, worldCoords);
  }

  /**
   * Rounds tile coords to the closest integer tile coords from the specified float tile coords.
   */
  public Vector2 toTile50(float x, float y, Vector2 dst) {
    x += tileOffset.x;
    y += tileOffset.y;
    dst.x = x < 0 ? MathUtils.round(x) : MathUtils.roundPositive(x);
    dst.y = y < 0 ? MathUtils.round(y) : MathUtils.roundPositive(y);
    return dst;
  }

  public Vector2 screenToWorld(float x, float y, Vector2 dst) {
    dst.set(x, y);
    unproject(dst);
    return toWorld(dst);
  }

  public Vector2 screenToTile(float x, float y, Vector2 dst) {
    screenToWorld(x, y, dst);
    return toTile(dst);
  }

  public Builder agg(Vector2 vec) {
    return builder.reset(vec);
  }

  public final class Builder {
    Vector2 vec;

    public Builder reset(Vector2 vec) {
      this.vec = vec;
      return this;
    }

    public Builder unproject() {
      IsometricCamera.this.unproject(vec);
      return this;
    }

    public Builder project() {
      IsometricCamera.this.project(vec);
      return this;
    }

    public Builder toScreen() {
      IsometricCamera.this.toScreen(vec);
      return this;
    }

    public Builder toWorld() {
      IsometricCamera.this.toWorld(vec);
      return this;
    }

    public Builder toTile() {
      IsometricCamera.this.toTile(vec);
      return this;
    }

    public Vector2 ret() {
      return vec;
    }
  }
}
