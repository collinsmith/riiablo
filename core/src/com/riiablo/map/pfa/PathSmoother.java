package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;

public class PathSmoother<N> {
  final RaycastCollisionDetector<Vector2> raycaster;
  final Ray<Vector2> ray = new Ray<>(new Vector2(), new Vector2());

  private static final Ray<Vector2> upper = new Ray<>(new Vector2(), new Vector2());
  private static final Ray<Vector2> lower = new Ray<>(new Vector2(), new Vector2());

  private static final Vector2 radius = new Vector2();
  private static final Vector2 normal = new Vector2();

  public PathSmoother(RaycastCollisionDetector<Vector2> raycaster) {
    this.raycaster = raycaster;
  }

  public int smoothPath(int flags, int size, SmoothableGraphPath<N, Vector2> path) {
    int length = path.getCount();
    if (length <= 2) return 0;

    int outId = 1;
    int inId  = 2;
    boolean collidesUpper;
    boolean collidesLower;
    while (inId < length) {
      ray.start.set(path.getNodePosition(outId - 1));
      ray.end.set(path.getNodePosition(inId));

      if (size <= 0) {
        collidesUpper = collidesLower = raycaster.collides(ray);
      } else {
        radius.set(ray.end).sub(ray.start).setLength(size / 2f);

        normal.set(radius).rotate90(-1);
        upper.start.set(ray.start).add(normal);
        upper.end.set(ray.end).add(normal);
        collidesUpper = raycaster.collides(upper);

        normal.set(radius).rotate90(1);
        lower.start.set(ray.start).add(normal);
        lower.end.set(ray.end).add(normal);
        collidesLower = raycaster.collides(lower);
      }

      if (collidesUpper || collidesLower) {
        path.swapNodes(outId, inId - 1);
        outId++;
      }

      inId++;
    }

    path.swapNodes(outId, inId - 1);
    path.truncatePath(outId + 1);
    return inId - outId - 1;
  }
}
