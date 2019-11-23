package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.riiablo.map.MapGraph;

//refactor of com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
public class AStarPathFinder implements PathFinder {
  enum UniformHeuristics implements Heuristic<Point2> {
    EUCLIDEAN() {
      final float DIAGONAL_COST = (float) Math.sqrt(2);

      @Override
      public float estimate(Point2 src, Point2 dst) {
        return src.x != dst.x && src.y != dst.y ? DIAGONAL_COST : 1;
      }
    },
    MANHATTAN() {
      final float DIAGONAL_COST = 2;

      @Override
      public float estimate(Point2 src, Point2 dst) {
        return src.x != dst.x && src.y != dst.y ? DIAGONAL_COST : 1;
      }
    }
  }
  static final UniformHeuristics uniformHeuristic = UniformHeuristics.EUCLIDEAN;

  enum Heuristics implements Heuristic<Point2> {
    EUCLIDEAN() {
      @Override
      public float estimate(Point2 src, Point2 dst) {
        return Vector2.dst(src.x, src.y, dst.x, dst.y);
      }
    },
    MANHATTAN() {
      @Override
      public float estimate(Point2 src, Point2 dst) {
        return Math.abs(dst.x - src.x) + Math.abs(dst.y - src.y);
      }
    }
  }
  static final Heuristics heuristic = Heuristics.EUCLIDEAN;


  MapGraph graph;
  Point2 current;
  BinaryHeap<Point2> openList = new BinaryHeap<>();
  public Metrics metrics;

  private int searchId;
  private final Array<Point2> neighbors = new Array<>(false, 8);

  private int size;

  public AStarPathFinder setSize(int size) {
    this.size = size;
    return this;
  }

  public AStarPathFinder(MapGraph graph) {
    this(graph, false);
  }

  public AStarPathFinder(MapGraph graph, boolean calculateMetrics) {
    this.graph = graph;
    if (calculateMetrics) this.metrics = new Metrics();
  }

  public boolean search(Point2 startNode, Point2 endNode, int flags, int size, GraphPath<Point2> outPath) {
    boolean found = search(startNode, endNode, flags, size);
    if (found) generateNodePath(startNode, outPath);
    return found;
  }


  protected boolean search(Point2 startNode, Point2 endNode, int flags, int size) {
    initSearch(startNode, endNode);
    int limit = 0;
    do {
      current = openList.pop();
      current.category = Point2.CLOSED;
      if (current == endNode) return true;
      visitChildren(current, endNode, flags, size);
      if (metrics != null) metrics.visitedNodes++;
    } while (openList.size > 0 && limit++ < 900);
    return false;
  }

  protected void initSearch(Point2 startNode, Point2 endNode) {
    if (metrics != null) metrics.reset();
    if (++searchId < 0) searchId = 1;
    openList.clear();

    reset(startNode);
    startNode.parent = null;
    startNode.g = 0f;
    addToOpenList(startNode, heuristic.estimate(startNode, endNode));

    current = null;
  }

  protected void visitChildren(Point2 startNode, Point2 endNode, int flags, int size) {
    Array<Point2> neighbors = graph.getNeighbors(startNode, flags, this.neighbors);
    for (Point2 neighbor : neighbors) {
      if (neighbor.clearance < size) continue;
      float g = startNode.g() + uniformHeuristic.estimate(startNode, neighbor);

      float h;
      reset(neighbor);
      switch (neighbor.category) {
        case Point2.UNVISITED:
          h = heuristic.estimate(neighbor, endNode);
          break;
        case Point2.OPEN:
          if (neighbor.g() <= g) continue;
          openList.remove(neighbor);
          h = neighbor.f() - neighbor.g();
          break;
        case Point2.CLOSED:
          if (neighbor.g() <= g) continue;
          h = neighbor.f() - neighbor.g();
          break;
        default:
          throw new AssertionError("Invalid nodeRecord category: " + neighbor.category);
      }

      neighbor.g = g;
      neighbor.parent = startNode;

      addToOpenList(neighbor, g + h);
    }
  }

  protected void generateNodePath(Point2 startNode, GraphPath<Point2> outPath) {
    while (current.parent != null) {
      outPath.add(current);
      current = current.parent;
    }
    outPath.add(startNode);
    outPath.reverse();
  }

  protected void addToOpenList(Point2 nodeRecord, float estimatedTotalCost) {
    openList.add(nodeRecord, estimatedTotalCost);
    nodeRecord.category = Point2.OPEN;
    if (metrics != null) {
      metrics.openListAdditions++;
      metrics.openListPeak = Math.max(metrics.openListPeak, openList.size);
    }
  }

  protected Point2 reset(Point2 src) {
    if (src.searchId != searchId) {
      src.reset();
      src.searchId = searchId;
    }
    return src;
  }

  public static class Metrics {
    public int visitedNodes;
    public int openListAdditions;
    public int openListPeak;

    public void reset() {
      visitedNodes = 0;
      openListAdditions = 0;
      openListPeak = 0;
    }
  }
}
