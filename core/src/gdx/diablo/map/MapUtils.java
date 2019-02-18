package gdx.diablo.map;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MapUtils {

  private MapUtils() {}

  public static GraphPath<Point2> path(Map map, Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    Point2 srcP = new Point2(src);
    Point2 dstP = new Point2(dst);
    Array<Point2> coords = new Array<>();
    Array<Point2> children = new Array<>(8);
    coords.add(dstP);
    boolean found = false;
    do {
      for (Point2 point : coords) {
        found = expand(map, srcP, point, coords, children);
        coords.addAll(children);
        if (found) break;
      }
    } while (!found);

    if (!found) {
      return path;
    }

    Point2 next = null;
    Point2 last = coords.removeIndex(coords.size - 1);
    while (true) {
      for (Point2 coord : coords) {
        if (last.adjacent(coord)) {
          if (next == null || coord.cost < next.cost) {
            next = coord;
          }
        }
      }

      path.add(next);
      last = next;
      next = null;
      if (dstP.x == last.x && dstP.y == last.y) break;
    }

    return path;
  }

  private static boolean expand(Map map, Point2 dst, Point2 src, Array<Point2> coords, Array<Point2> children) {
    children.clear();
    if (check(map, dst, src, src.x - 1, src.y - 1, coords, children)) return true;
    if (check(map, dst, src, src.x - 1, src.y    , coords, children)) return true;
    if (check(map, dst, src, src.x - 1, src.y + 1, coords, children)) return true;
    if (check(map, dst, src, src.x    , src.y - 1, coords, children)) return true;
    if (check(map, dst, src, src.x    , src.y    , coords, children)) return true;
    if (check(map, dst, src, src.x    , src.y + 1, coords, children)) return true;
    if (check(map, dst, src, src.x + 1, src.y - 1, coords, children)) return true;
    if (check(map, dst, src, src.x + 1, src.y    , coords, children)) return true;
    if (check(map, dst, src, src.x + 1, src.y + 1, coords, children)) return true;
    return false;
  }

  private static boolean check(Map map, Point2 dst, Point2 src, int x, int y, Array<Point2> coords, Array<Point2> children) {
    Map.Zone zone = map.getZone(x, y);
    if (zone != null) {
      if (zone.flags(x, y) == 0) {
        float cost = src.cost;
        cost += (x != src.x && y != src.y) ? 1.414213562373095f : 1;
        if (!contains(coords, x, y, cost)) {
          Point2 point = new Point2(x, y, cost);
          children.add(point);
          if (dst.x == x && dst.y == y) return true;
        }
      }
    }

    return false;
  }

  private static boolean contains(Array<Point2> coords, int x, int y, float cost) {
    for (Point2 point : new Array.ArrayIterator<>(coords)) {
      if (point.x == x && point.y == y && point.cost <= cost) {
        return true;
      }
    }

    return false;
  }

  static class Point2 {
    int x;
    int y;
    float cost;

    Point2(int x, int y, float cost) {
      this.x = x;
      this.y = y;
      this.cost = cost;
    }

    Point2(Vector3 src) {
      x = (int) src.x;
      y = (int) src.y;
    }

    boolean adjacent(Point2 other) {
      return Vector2.dst2(x, y, other.x, other.y) < 2;
    }
  }

  /*
  public static Array<Vector3> path(Map map, Vector3 src, Vector3 dst) {
    ObjectSet<Vector3> open   = new ObjectSet<>();
    ObjectSet<Vector3> closed = new ObjectSet<>();
    open.add(src);

    int x, y;
    while (!open.isEmpty()) {
      Node cur = null;
      if (cur.data.equals(dst)) {
        return null;
      }

      x = (int) cur.data.x;
      y = (int) cur.data.y;

      Array<Node> neighbors = getNeighbors(map, cur, x, y);
      for (Node neighbor : neighbors) {
        float score = cur.cost + neighbor.cost;
      }
    }

    return null;
  }

  private static Array<Node> getNeighbors(Map map, Node n, int x, int y) {
    Array<Node> neighbors = new Array<>(8);
    addIfValid(neighbors, map, n, x - 1, y - 1);
    addIfValid(neighbors, map, n, x - 1, y    );
    addIfValid(neighbors, map, n, x - 1, y + 1);
    addIfValid(neighbors, map, n, x    , y - 1);
    addIfValid(neighbors, map, n, x    , y    );
    addIfValid(neighbors, map, n, x    , y + 1);
    addIfValid(neighbors, map, n, x + 1, y - 1);
    addIfValid(neighbors, map, n, x + 1, y    );
    addIfValid(neighbors, map, n, x + 1, y + 1);
    return neighbors;
  }

  private static boolean addIfValid(Array<Node> arr, Map map, Node n, int x, int y) {
    Map.Zone zone = map.getZone(x, y);
    if (zone != null) {
      if (zone.flags(x, y) == 0) {
        Node node = new Node();
        node.prev = n;
        node.data = new Vector3(x, y, 0);
        if (x != node.data.x && y != node.data.y) {
          node.cost = 1.414213562373095f;
        } else {
          node.cost = 1;
        }

        arr.add(node);
      }
    }

    return false;
  }

  static class Node {
    Node prev;
    Vector3 data;
    float cost;
    float score;

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (obj == this) return true;
      if (!(obj instanceof Node)) return false;
      return data.equals(((Node) obj).data);
    }

    @Override
    public int hashCode() {
      return data.hashCode();
    }
  }
  */
}
