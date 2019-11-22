package com.riiablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.Graph;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.pfa.ClearancedNode;
import com.riiablo.map.pfa.IndexedAStarPathFinder;
import com.riiablo.map.pfa.IndexedNode;
import com.riiablo.map.pfa.PathSmoother;
import com.riiablo.map.pfa.SimpleRaycastCollisionDetector;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;

import java.util.Iterator;

public class MapGraph implements Graph<MapGraph.Point2> {
  private static final String TAG = "MapGraph";
  private static final boolean DEBUG         = true;
  private static final boolean DEBUG_METRICS = DEBUG && !true;

  Heuristic<Point2> heuristic = new EuclideanDistanceHeuristic();

  Map                            map;
  SimpleRaycastCollisionDetector rayCaster;
  PathSmoother<Point2>           pathSmoother;

  final Point2 tmpPoint = new Point2();
  final Vector2 tmpVec = new Vector2();

  int index = 0;
  final ObjectSet<Point2> identity = new ObjectSet<>();
  private static final int[][][] NEAR = {
      { // 1
        { 0,  0}
      },
      { // 2
        {-1, -1},
        { 0, -1},
        { 1, -1},

        {-1,  0},
      //{ 0,  0},
        { 1,  0},

        {-1,  1},
        { 0,  1},
        { 1,  1},
      },
      { // 3
        {-1, -2},
        { 0, -2},
        { 1, -2},

        {-2, -1},
      //{-1, -1},
      //{ 0, -1},
      //{ 1, -1},
        { 2, -1},

        {-2,  0},
      //{-1,  0},
      //{ 0,  0},
      //{ 1,  0},
        { 2,  0},

        {-2,  1},
      //{-1,  1},
      //{ 0,  1},
      //{ 1,  1},
        { 2,  1},

        {-1,  2},
        { 0,  2},
        { 1,  2},
      }
  };

  public MapGraph(Map map) {
    this.map = map;
    rayCaster = new SimpleRaycastCollisionDetector(map);
    pathSmoother = new PathSmoother<>(rayCaster);
  }

  private Point2 getOrCreate(Vector2 src) {
    Point2 existing = identity.get(tmpPoint.set(src));
    if (existing == null) {
      existing = new Point2(src);
      identity.add(existing);
      existing.index = index++;
      calculateClearance(existing);
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

  public void smoothPath(int size, SmoothableGraphPath<Point2, Vector2> path) {
    pathSmoother.smoothPath(size, path);
  }

  @Override
  public Array<Connection<Point2>> getConnections(Point2 src) {
    Array<Connection<Point2>> connections = src.connections;
    if (connections == null) {
      connections = src.connections = new Array<>(false, 8);
      tryConnect(connections, src, src.x - 1, src.y);
      tryConnect(connections, src, src.x, src.y - 1);
      tryConnect(connections, src, src.x, src.y + 1);
      tryConnect(connections, src, src.x + 1, src.y);

      tryConnect(connections, src, src.x - 1, src.y - 1);
      tryConnect(connections, src, src.x - 1, src.y + 1);
      tryConnect(connections, src, src.x + 1, src.y - 1);
      tryConnect(connections, src, src.x + 1, src.y + 1);
    }


    return connections;
  }

  private void tryConnect(Array<Connection<Point2>> connections, Point2 src, int x, int y) {
    if (map.flags(x, y) != 0) return;
    Point2 dst = getOrCreate(tmpVec.set(x, y));
    connections.add(new Path(src, dst));
  }

  private void calculateClearance(Point2 node) {
    byte i;
size:
    for (i = 0; i < NEAR.length; i++) {
      for (int[] p : NEAR[i]) {
        if (map.flags(node.x + p[0], node.y + p[1]) != 0) {
          break size;
        }
      }
    }

    node.clearance = i;
  }

  public static class Point2 implements IndexedNode, ClearancedNode {
    public int x;
    public int y;

    int index;
    byte clearance;
    Array<Connection<Point2>> connections;

    Point2() {}

    Point2(Vector2 src) {
      set(src);
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public int getClearance() {
      return clearance;
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
      return equals((Point2) obj);
    }

    public boolean equals(Point2 other) {
      return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class Path extends DefaultConnection<Point2> {
    static final float DIAGONAL_COST = (float)Math.sqrt(2);

    Path() {
      super(null, null);
    }

    Path(Point2 src, Point2 dst) {
      super(src, dst);
    }

    Path set(Point2 src, Point2 dst) {
      fromNode = src;
      toNode = dst;
      return this;
    }

    @Override
    public float getCost() {
      return fromNode.x != toNode.x && fromNode.y != toNode.y ? DIAGONAL_COST : 1;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + fromNode.hashCode();
      result = prime * result + toNode.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null) return false;
      if (!(obj instanceof Path)) return false;
      Path other = (Path) obj;
      return fromNode.equals(other.fromNode) && toNode.equals(other.toNode);
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

    public Iterator<Vector2> vectorIterator() {
      return IteratorUtils.transformedIterator(new Array.ArrayIterator<>(nodes), new Transformer<Point2, Vector2>() {
        final Vector2 tmp = new Vector2();

        @Override
        public Vector2 transform(Point2 input) {
          return tmp.set(input.x, input.y);
        }
      });
    }
  }
}
