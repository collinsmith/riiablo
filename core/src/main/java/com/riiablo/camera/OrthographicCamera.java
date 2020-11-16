package com.riiablo.camera;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class OrthographicCamera extends com.badlogic.gdx.graphics.OrthographicCamera {

  private final Vector3 tmpVec3 = new Vector3();

  public OrthographicCamera() {}

  public Vector2 project(Vector2 worldCoords) {
    return project(worldCoords.x, worldCoords.y, worldCoords);
  }

  public Vector2 project(float x, float y, Vector2 dst) {
    tmpVec3.set(x, y, 0);
    project(tmpVec3);
    return dst.set(tmpVec3.x, tmpVec3.y);
  }

  public Vector2 unproject(Vector2 screenCoords) {
    return unproject(screenCoords.x, screenCoords.y, screenCoords);
  }

  public Vector2 unproject(float x, float y, Vector2 dst) {
    tmpVec3.set(x, y, 0);
    unproject(tmpVec3);
    return dst.set(tmpVec3.x, tmpVec3.y);
  }
}
