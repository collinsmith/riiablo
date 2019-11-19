package com.riiablo.map;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

public class MapGraph implements IndexedGraph<MapGraph.Point2> {
  private static final String TAG = "MapGraph";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_METRICS = DEBUG && !true;

  Heuristic<Point2> heuristic = new EuclideanDistanceHeuristic();

  Map                           map;
  MapRaycastCollisionDetector   rayCaster;
  PathSmoother<Point2, Vector2> pathSmoother;


  final Point2 tmpPoint = new Point2();
  final Vector2 tmpVec = new Vector2();

  int index = 0;
  final ObjectIntMap<Point2> indexes = new ObjectIntMap<>();

  final ObjectSet<Point2> identity = new ObjectSet<>();
  final Array<Connection<Point2>> connections = new Array<>(false, 8);

  public MapGraph(Map map) {
    this.map = map;
    rayCaster = new MapRaycastCollisionDetector(this);
    pathSmoother = new PathSmoother<>(rayCaster);
  }

  private Point2 getOrCreate(Vector2 src) {
    Point2 existing = identity.get(tmpPoint.set(src));
    if (existing == null) {
      existing = new Point2(src);
      identity.add(existing);
      indexes.put(existing, index++);
    }

    return existing;
  }

  public GraphPath<Point2> path(Vector2 src, Vector2 dst, GraphPath<Point2> path) {
    searchNodePath(new IndexedAStarPathFinder<>(this), src, dst, path);
    return path;
  }

  public GraphPath<Point2> path(Point2 src, Point2 dst, GraphPath<Point2> path) {
    searchNodePath(new IndexedAStarPathFinder<>(this), src, dst, path);
    return path;
  }

  public boolean searchNodePath(PathFinder<Point2> pathFinder, Vector2 src, Vector2 dst, GraphPath<Point2> path) {
    path.clear();
    if (dst == null) return false;
    if (map.flags(dst) != 0) return false;
    Point2 srcP = getOrCreate(src);
    Point2 dstP = getOrCreate(dst);
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
    return indexes.get(node, -1);
  }

  @Override
  public int getNodeCount() {
    return 2 << 20;
  }

  @Override
  public Array<Connection<Point2>> getConnections(Point2 src) {
    connections.clear();
    tryConnect(connections, src, src.x - 1, src.y - 1);
    tryConnect(connections, src, src.x - 1, src.y    );
    tryConnect(connections, src, src.x - 1, src.y + 1);
    tryConnect(connections, src, src.x    , src.y - 1);
    tryConnect(connections, src, src.x    , src.y + 1);
    tryConnect(connections, src, src.x + 1, src.y - 1);
    tryConnect(connections, src, src.x + 1, src.y    );
    tryConnect(connections, src, src.x + 1, src.y + 1);
    return connections;
  }

  private void tryConnect(Array<Connection<Point2>> connections, Point2 src, int x, int y) {
    if (map.flags(tmpVec.set(x, y)) != 0) return;
    Point2 dst = getOrCreate(tmpVec);
    connections.add(new Path(src, dst));
  }

  public static class Point2 {
    public int x;
    public int y;

    Point2() {}

    Point2(Vector2 src) {
      set(src);
    }

    Point2 set(Vector2 src) {
      x = Map.round(src.x);
      y = Map.round(src.y);
      return this;
    }

    @Override
    public int hashCode() {
      return (x * 73856093) ^ (y * 83492791);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null) return false;
      if (!(obj instanceof Point2)) return false;
      Point2 other = (Point2) obj;
      return x == other.x && y == other.y;
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

  public static class MapGraphPath extends DefaultGraphPath<Point2> implements SmoothableGraphPath<Point2, Vector2>, Pool.Poolable {
    private Vector2 tmp = new Vector2();

    @Override
    public void reset() {
      clear();
    }

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
    Map      map;
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
