package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.Map;

public class SimpleRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
  private static final int SAMPLES = 5; // points per unit
  private static final float DELTA = 1f / SAMPLES;

  private static final Vector2 delta = new Vector2();
  private static final Vector2 sample = new Vector2();

  private Map map;

  public SimpleRaycastCollisionDetector(Map map) {
    this.map = map;
  }

  @Override
  public boolean collides(Ray<Vector2> ray) {
    Vector2 start = ray.start;
    Vector2 end = ray.end;

    sample.set(start);
    delta.set(end).sub(start).setLength(DELTA);
    float add = delta.len();
    for (float curDist = 0, maxDist = start.dst(end); curDist < maxDist; curDist += add, sample.add(delta)) {
      if (map.flags(sample) != 0) return true;
    }

    return false;
  }


  @Override
  public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
    return true;
  }
}
