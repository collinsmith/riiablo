package gdx.diablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class MapGraph implements IndexedGraph<MapGraph.Point2> {
  private static final String TAG = "MapGraph";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_METRICS = DEBUG && !true;

  Heuristic<Point2> heuristic = new EuclideanDistanceHeuristic();

  Map map;
  IntMap<Point2> points = new IntMap<>();
  MapRaycastCollisionDetector rayCaster;
  PathSmoother<Point2, Vector2> pathSmoother;

  public MapGraph(Map map) {
    this.map = map;
    rayCaster = new MapRaycastCollisionDetector(this);
    pathSmoother = new PathSmoother<>(rayCaster);
  }

  public GraphPath<Point2> path(Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    Map.Zone zone = map.getZone((int) dst.x, (int) dst.y);
    if (zone != null && zone.flags((int) dst.x, (int) dst.y) != 0) {
      return path;
    }

    int hash = Point2.hash(src);
    Point2 srcP = points.get(hash);
    if (srcP == null) {
      srcP = new Point2(src);
      points.put(hash, srcP);
    }
    hash = Point2.hash(dst);
    Point2 dstP = points.get(hash);
    if (dstP == null) {
      dstP = new Point2(dst);
      points.put(hash, dstP);
    }
    return path(srcP, dstP, path);
  }

  public GraphPath<Point2> path(Point2 src, Point2 dst, GraphPath<Point2> path) {
    path.clear();
    new IndexedAStarPathFinder<>(this).searchNodePath(src, dst, heuristic, path);
    return path;
  }

  public boolean searchNodePath(PathFinder<Point2> pathFinder, Vector3 src, Vector3 dst, GraphPath<Point2> path) {
    path.clear();
    if (dst == null) return false;
    //Map.Zone zone = map.getZone((int) dst.x, (int) dst.y);
    //if (zone != null && zone.flags((int) dst.x, (int) dst.y) != 0) {
    int x = Map.round(dst.x);
    int y = Map.round(dst.y);
    Map.Zone zone = map.getZone(x, y);
    if (zone != null && zone.flags(x, y) != 0) {
      return false;
    }

    int hash = Point2.hash(src);
    Point2 srcP = points.get(hash);
    if (srcP == null) {
      srcP = new Point2(src);
      points.put(hash, srcP);
    }
    hash = Point2.hash(dst);
    Point2 dstP = points.get(hash);
    if (dstP == null) {
      dstP = new Point2(dst);
      points.put(hash, dstP);
    }
    return searchNodePath(pathFinder, srcP, dstP, path);
  }

  public boolean searchNodePath(PathFinder<Point2> pathFinder, Point2 src, Point2 dst, GraphPath<Point2> path) {
    boolean success = pathFinder.searchNodePath(src, dst, heuristic, path);
    if (DEBUG_METRICS && pathFinder instanceof IndexedAStarPathFinder) {
      IndexedAStarPathFinder.Metrics metrics = ((IndexedAStarPathFinder) pathFinder).metrics;
      Gdx.app.debug(TAG, String.format("visitedNodes=%d, openListAdditions=%d, openListPeak=%d",
          metrics.visitedNodes, metrics.openListAdditions, metrics.openListPeak));
    }

    return success;
  }

  public void smoothPath(SmoothableGraphPath<Point2, Vector2> path) {
    pathSmoother.smoothPath(path);
  }

  @Override
  public int getIndex(Point2 node) {
    return node.index;
  }

  @Override
  public int getNodeCount() {
    return 2 << 20;
  }

  @Override
  public Array<Connection<Point2>> getConnections(Point2 src) {
    Array<Connection<Point2>> connections = src.connections;
    if (connections == null) {
      connections = src.connections = new Array<>(8);
      tryConnect(src, src.x - 1, src.y - 1);
      tryConnect(src, src.x - 1, src.y    );
      tryConnect(src, src.x - 1, src.y + 1);
      tryConnect(src, src.x    , src.y - 1);
      tryConnect(src, src.x    , src.y + 1);
      tryConnect(src, src.x + 1, src.y - 1);
      tryConnect(src, src.x + 1, src.y    );
      tryConnect(src, src.x + 1, src.y + 1);
    }

    return connections;
  }

  private void tryConnect(Point2 src, int x, int y) {
    Map.Zone zone = map.getZone(x, y);
    if (zone != null && zone.flags(x, y) == 0) {
      final int hash = Point2.hash(x, y);
      Point2 dst = points.get(hash);
      if (dst == null) {
        dst = new Point2(x, y);
        points.put(hash, dst);
      }
      src.connections.add(new Path(src, dst));
    }
  }

  public static class Point2 {
    public int x;
    public int y;
    int index;
    Array<Connection<Point2>> connections;

    static int indexes = 0;

    Point2(int x, int y) {
      this.x = x;
      this.y = y;
      index = indexes++;
    }

    Point2(Vector3 src) {
      //this((int) src.x, (int) src.y);
      this(Map.round(src.x), Map.round(src.y));
    }

    @Override
    public int hashCode() {
      return hash(x, y);
    }

    static int hash(Vector3 src) {
      //return hash((int) src.x, (int) src.y);
      return hash(Map.round(src.x), Map.round(src.y));
    }

    static int hash(int x, int y) {
      return (x * 73856093) ^ (y * 83492791);
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class Path extends DefaultConnection<Point2> {
    static final float DIAGONAL_COST = (float)Math.sqrt(2);

    Path(Point2 src, Point2 dst) {
      super(src, dst);
    }

    @Override
    public float getCost() {
      return fromNode.x != toNode.x && fromNode.y != toNode.y ? DIAGONAL_COST : 1;
    }
  }

  static class ManhattanDistanceHeuristic implements Heuristic<Point2> {
    @Override
    public float estimate(Point2 src, Point2 dst) {
      return Math.abs(dst.x - src.x) + Math.abs(dst.y - src.y);
    }
  }

  static class EuclideanDistanceHeuristic implements Heuristic<Point2> {
    @Override
    public float estimate(Point2 src, Point2 dst) {
      return Vector2.dst(src.x, src.y, dst.x, dst.y);
    }
  }

  public static class MapGraphPath extends DefaultGraphPath<Point2> implements SmoothableGraphPath<Point2, Vector2> {
    private Vector2 tmp = new Vector2();

    public boolean isEmpty() {
      return nodes.isEmpty();
    }

    @Override
    public Vector2 getNodePosition(int index) {
      Point2 src = nodes.get(index);
      return tmp.set(src.x, src.y);
    }

    @Override
    public void swapNodes(int index1, int index2) {
      nodes.set(index1, nodes.get(index2));
    }

    @Override
    public void truncatePath(int newLength) {
      nodes.truncate(newLength);
    }

    @Override
    public String toString() {
      return nodes.toString();
    }
  }

  static class MapRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
    Map map;
    MapGraph mapGraph;

    public MapRaycastCollisionDetector(MapGraph mapGraph) {
      this.mapGraph = mapGraph;
      this.map = mapGraph.map;
    }

    @Override
    public boolean collides(Ray<Vector2> ray) {
      int x0 = (int) ray.start.x;
      int y0 = (int) ray.start.y;
      int x1 = (int) ray.end.x;
      int y1 = (int) ray.end.y;

      int tmp;
      boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
      if (steep) {
        // Swap x0 and y0
        tmp = x0;
        x0 = y0;
        y0 = tmp;
        // Swap x1 and y1
        tmp = x1;
        x1 = y1;
        y1 = tmp;
      }
      if (x0 > x1) {
        // Swap x0 and x1
        tmp = x0;
        x0 = x1;
        x1 = tmp;
        // Swap y0 and y1
        tmp = y0;
        y0 = y1;
        y1 = tmp;
      }

      int deltax = x1 - x0;
      int deltay = Math.abs(y1 - y0);
      int error = 0;
      int y = y0;
      int ystep = (y0 < y1 ? 1 : -1);
      for (int x = x0; x <= x1; x++) {
        // TODO: Why is this distinction needed?
        if (steep) {
          Map.Zone zone = map.getZone(y, x);
          if (zone == null || zone.flags(y, x) != 0) return true; // We've hit a wall
        } else {
          Map.Zone zone = map.getZone(x, y);
          if (zone == null || zone.flags(x, y) != 0) return true; // We've hit a wall
        }
        error += deltay;
        if (error + error >= deltax) {
          y += ystep;
          error -= deltax;
        }
      }

      return false;
    }

    @Override
    public boolean findCollision(Collision<Vector2> dst, Ray<Vector2> ray) {
      int x0 = (int) ray.start.x;
      int y0 = (int) ray.start.y;
      int x1 = (int) ray.end.x;
      int y1 = (int) ray.end.y;

      int tmp;
      boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
      if (steep) {
        // Swap x0 and y0
        tmp = x0;
        x0 = y0;
        y0 = tmp;
        // Swap x1 and y1
        tmp = x1;
        x1 = y1;
        y1 = tmp;
      }
      if (x0 > x1) {
        // Swap x0 and x1
        tmp = x0;
        x0 = x1;
        x1 = tmp;
        // Swap y0 and y1
        tmp = y0;
        y0 = y1;
        y1 = tmp;
      }

      int deltax = x1 - x0;
      int deltay = Math.abs(y1 - y0);
      int error = 0;
      int y = y0;
      int ystep = (y0 < y1 ? 1 : -1);
      dst.point.set(steep ? y0 : x0, steep ? x0 : y0);
      //dst.normal.setZero();
      for (int x = x0; x <= x1; x++) {
        if (steep) {
          Map.Zone zone = map.getZone(y, x);
          if (zone == null || zone.flags(y, x) != 0) {
            //dst.normal.set(y, x);
            return true;
          }
        } else {
          Map.Zone zone = map.getZone(x, y);
          if (zone == null || zone.flags(x, y) != 0) {
            //dst.normal.set(x, y);
            return true;
          }
        }
        dst.point.set(steep ? y : x, steep ? x : y);
        error += deltay;
        if (error + error >= deltax) {
          y += ystep;
          error -= deltax;
        }
      }

      return false;
    }
  }
}
