package gdx.diablo.map;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectMap;

public class MapPather {
  Map map;
  Heuristic heuristic = new DiagonalHeuristic();

  public MapPather(Map map) {
    this.map = map;
  }

  public boolean path(Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    if (src.dst(dst) > 25f) return false;
    return path(new Point2(src), new Point2(dst), path);
  }

  public boolean path(Point2 src, Point2 dst, GraphPath<Point2> path) {
    path.clear();
    Map.Zone zone = map.getZone(dst.x, dst.y);
    if (zone == null || zone.flags(dst.x, dst.y) != 0) return false;

    BinaryHeap<Point2> closedSet = new BinaryHeap<>();
    BinaryHeap<Point2> openSet   = new BinaryHeap<>();
    openSet.add(src);

    closedSet.contains(dst, false);

    ObjectMap<Point2, Point2> cameFrom = new ObjectMap<>();

    ObjectFloatMap<Point2> gScore = new ObjectFloatMap<>();
    gScore.put(src, 0);

    ObjectFloatMap<Point2> fScore = new ObjectFloatMap<>();
    fScore.put(src, heuristic.estimate(src, dst));

    Array<Point2> neighbors = new Array<>(8);

    while (openSet.size > 0) {
      Point2 current = openSet.pop();
      closedSet.add(current);
      if (current.equals(dst)) {
        buildPath(current, cameFrom, path);
        return true;
      }

      getNeighbors(current, neighbors);
      for (Point2 neighbor : neighbors) {
        if (closedSet.contains(neighbor, false)) {
          continue;
        }

        float tent_gScore = gScore.get(current, Float.POSITIVE_INFINITY) + Point2.dst(current, neighbor);
        if (!openSet.contains(neighbor, false)) {
          openSet.add(neighbor);
        } else if (tent_gScore >= gScore.get(neighbor, Float.POSITIVE_INFINITY)) {
          continue;
        }

        cameFrom.put(neighbor, current);
        gScore.put(neighbor, tent_gScore);
        fScore.put(neighbor, gScore.get(neighbor, Float.POSITIVE_INFINITY) + heuristic.estimate(neighbor, dst));
      }
    }

    return false;
  }

  private void buildPath(Point2 src, ObjectMap<Point2, Point2> cameFrom, GraphPath<Point2> path) {
    path.add(src);
    while (cameFrom.containsKey(src)) {
      src = cameFrom.get(src);
      path.add(src);
    }
  }

  private void getNeighbors(Point2 src, Array<Point2> dst) {
    dst.size = 0;
    addNeighbor(src, src.x - 1, src.y - 1, dst);
    addNeighbor(src, src.x - 1, src.y    , dst);
    addNeighbor(src, src.x - 1, src.y + 1, dst);
    addNeighbor(src, src.x    , src.y - 1, dst);
    addNeighbor(src, src.x    , src.y + 1, dst);
    addNeighbor(src, src.x + 1, src.y - 1, dst);
    addNeighbor(src, src.x + 1, src.y    , dst);
    addNeighbor(src, src.x + 1, src.y + 1, dst);
  }

  private void addNeighbor(Point2 src, int x, int y, Array<Point2> dst) {
    Map.Zone zone = map.getZone(x, y);
    if (zone == null) return;
    if (zone.flags(x, y) == 0) {
      float cost = src.getValue() + ((x != src.x && y != src.y) ? 1.414213562373095f : 1f);
      Point2 point = new Point2(x, y, cost);
      dst.add(point);
    }
  }

  interface Heuristic {
	float estimate(Point2 src, Point2 dst);
  }

  static class DiagonalHeuristic implements Heuristic {
    @Override
    public float estimate(Point2 src, Point2 dst) {
      int dx = Math.abs(src.x - dst.x);
      int dy = Math.abs(src.y - dst.y);
      return 1f * Math.max(dx, dy) + (1.414213562373095f-1f) * Math.min(dx, dy);
    }
  }

  static class EuclideanHeuristic implements Heuristic {
    @Override
    public float estimate(Point2 src, Point2 dst) {
      return Point2.dst(src, dst);
    }
  }
}
