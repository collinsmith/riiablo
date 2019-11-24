package com.riiablo.map;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.riiablo.map.pfa.PathFinder;
import com.riiablo.map.pfa.PathSmoother;
import com.riiablo.map.pfa.Point2;
import com.riiablo.map.pfa.RaycastCollisionDetector;

public class MapGraph {
  Map map;
  PathSmoother<Point2> smoother;
  RaycastCollisionDetector raycaster;

  final Point2 tmpPoint = new Point2();
  final ObjectSet<Point2> identity = new ObjectSet<>();

  public MapGraph(Map map) {
    this.map = map;
    raycaster = new RaycastCollisionDetector(map, this);
    smoother = new PathSmoother<>(raycaster);
  }

  public Point2 getOrCreate(Vector2 src) {
    return getOrCreate(tmpPoint.set(src));
  }

  public Point2 getOrCreate(int x, int y) {
    return getOrCreate(tmpPoint.set(x, y));
  }

  private Point2 getOrCreate(Point2 src) {
    Point2 existing = identity.get(src);
    if (existing == null) {
      existing = new Point2(src);
      identity.add(existing);
      existing.updateClearance(map, 0);
    }

    return existing;
  }

  public boolean searchNodePath(PathFinder pathFinder, Vector2 src, Vector2 dst, int flags, int size, GraphPath<Point2> outPath) {
    outPath.clear();
    if (dst == null) return false;
    if (map.flags(dst) != 0) return false;
    Point2 srcP = getOrCreate(src);
    Point2 dstP = getOrCreate(dst);
    return searchNodePath(pathFinder, srcP, dstP, flags, size, outPath);
  }

  boolean searchNodePath(PathFinder pathFinder, Point2 src, Point2 dst, int flags, int size, GraphPath<Point2> outPath) {
    return pathFinder.search(src, dst, flags, size, outPath);
  }

  public void smoothPath(int flags, int size, SmoothableGraphPath<Point2, Vector2> path) {
    smoother.smoothPath(flags, size, path);
  }

  public Array<Point2> getNeighbors(Point2 src, int flags, Array<Point2> neighbors) {
    neighbors.clear();
    tryNeighbor(neighbors, flags, src.x - 1, src.y    );
    tryNeighbor(neighbors, flags, src.x    , src.y - 1);
    tryNeighbor(neighbors, flags, src.x    , src.y + 1);
    tryNeighbor(neighbors, flags, src.x + 1, src.y    );

    tryNeighbor(neighbors, flags, src.x - 1, src.y - 1);
    tryNeighbor(neighbors, flags, src.x - 1, src.y + 1);
    tryNeighbor(neighbors, flags, src.x + 1, src.y - 1);
    tryNeighbor(neighbors, flags, src.x + 1, src.y + 1);
    return neighbors;
  }

  public boolean tryNeighbor(Array<Point2> neighbors, int flags, int x, int y) {
    if (!isWalkable(x, y, flags)) return false;
    Point2 point = getOrCreate(x, y);
    neighbors.add(point);
    return true;
  }

  public boolean isWalkable(int x, int y, int flags) {
    return map.flags(x, y) == 0;
  }
}
