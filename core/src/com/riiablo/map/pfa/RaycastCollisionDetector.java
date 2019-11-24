package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.Map;
import com.riiablo.map.MapGraph;

public class RaycastCollisionDetector {
  private static final int SAMPLES = 5; // points per unit
  private static final float DELTA = 1f / SAMPLES;

  private final Vector2 delta = new Vector2();
  private final Vector2 sample = new Vector2();

  private Map map;
  private MapGraph graph;

  public RaycastCollisionDetector(Map map, MapGraph graph) {
    this.map = map;
    this.graph = graph;
  }

  public boolean collides(Ray<Vector2> ray, int flags, int size) {
    Vector2 start = ray.start;
    Vector2 end = ray.end;

    sample.set(start);
    delta.set(end).sub(start).setLength(DELTA);
    float add = delta.len();
    for (float curDist = 0, maxDist = start.dst(end); curDist < maxDist; curDist += add, sample.add(delta)) {
      if (map.flags(sample) != 0) return true;
    }

    return map.flags(end) != 0;
  }


  public boolean findCollision(Ray<Vector2> ray, int flags, int size, Collision<Vector2> collision) {
    Vector2 start = ray.start;
    Vector2 end = ray.end;

    Vector2 last = collision.point.setZero();
    Vector2 normal = collision.normal.setZero(); // TODO: calculate normal

    sample.set(start);
    delta.set(end).sub(start).setLength(DELTA);
    float add = delta.len();
    for (float curDist = 0, maxDist = start.dst(end); curDist < maxDist; curDist += add, last.set(sample), sample.add(delta)) {
      if (map.flags(sample) != 0 || graph.getOrCreate(sample).clearance < size) {
        return true;
      }
    }

    return false;
  }
}