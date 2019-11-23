package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.riiablo.map.MapGraph;

import java.util.Arrays;

//refactor of com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
public class IndexedAStarPathFinder implements PathFinder {
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
  NodeRecord current;
  NodeRecord[] nodeRecords = new NodeRecord[16384];
  BinaryHeap<NodeRecord> openList = new BinaryHeap<>();
  public Metrics metrics;

  private int searchId;
  private final Array<Point2> neighbors = new Array<>(false, 8);

  private static final byte UNVISITED = 0;
  private static final byte OPEN      = 1;
  private static final byte CLOSED    = 2;

  private int size;

  public IndexedAStarPathFinder setSize(int size) {
    this.size = size;
    return this;
  }

  public IndexedAStarPathFinder(MapGraph graph) {
    this(graph, false);
  }

  public IndexedAStarPathFinder(MapGraph graph, boolean calculateMetrics) {
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
      current.category = CLOSED;
      if (current.node == endNode) return true;
      visitChildren(current.node, endNode, flags, size);
      if (metrics != null) metrics.visitedNodes++;
    } while (openList.size > 0 && limit++ < 900);
    return false;
  }

  protected void initSearch(Point2 startNode, Point2 endNode) {
    if (metrics != null) metrics.reset();
    if (++searchId < 0) searchId = 1;
    openList.clear();

    NodeRecord startRecord = getNodeRecord(startNode);
    startRecord.node = startNode;
    startRecord.parent = null;
    startRecord.costSoFar = 0f;
    addToOpenList(startRecord, heuristic.estimate(startNode, endNode));

    current = null;
  }

  protected void visitChildren(Point2 startNode, Point2 endNode, int flags, int size) {
    Array<Point2> neighbors = graph.getNeighbors(startNode, flags, this.neighbors);
    for (Point2 neighbor : neighbors) {
      if (neighbor.clearance < size) continue;
      float nodeCost = current.costSoFar + uniformHeuristic.estimate(startNode, neighbor);

      float nodeHeuristic;
      NodeRecord nodeRecord = getNodeRecord(neighbor);
      switch (nodeRecord.category) {
        case UNVISITED:
          nodeHeuristic = heuristic.estimate(neighbor, endNode);
          break;
        case OPEN:
          if (nodeRecord.costSoFar <= nodeCost) continue;
          openList.remove(nodeRecord);
          nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
          break;
        case CLOSED:
          if (nodeRecord.costSoFar <= nodeCost) continue;
          nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
          break;
        default:
          throw new AssertionError("Invalid nodeRecord category: " + nodeRecord.category);
      }

      nodeRecord.costSoFar = nodeCost;
      nodeRecord.parent = startNode;

      addToOpenList(nodeRecord, nodeCost + nodeHeuristic);
    }
  }

  protected void generateNodePath(Point2 startNode, GraphPath<Point2> outPath) {
    while (current.parent != null) {
      outPath.add(current.node);
      current = nodeRecords[current.parent.index];
    }
    outPath.add(startNode);
    outPath.reverse();
  }

  protected void addToOpenList(NodeRecord nodeRecord, float estimatedTotalCost) {
    openList.add(nodeRecord, estimatedTotalCost);
    nodeRecord.category = OPEN;
    if (metrics != null) {
      metrics.openListAdditions++;
      metrics.openListPeak = Math.max(metrics.openListPeak, openList.size);
    }
  }

  private void resize(int newSize) {
    nodeRecords = Arrays.copyOf(nodeRecords, newSize);
  }

  protected NodeRecord getNodeRecord(Point2 node) {
    int index = node.index;
    if (index >= nodeRecords.length) resize((int) (nodeRecords.length * 1.75f));
    NodeRecord nr = nodeRecords[index];
    if (nr != null) {
      if (nr.searchId != searchId) {
        nr.category = UNVISITED;
        nr.searchId = searchId;
      }
      return nr;
    }
    nr = nodeRecords[index] = new NodeRecord();
    nr.node = node;
    nr.searchId = searchId;
    return nr;
  }

  static class NodeRecord extends BinaryHeap.Node {
    Point2 node;
    Point2 parent;
    float costSoFar;
    byte category;
    int searchId;

    public NodeRecord() {
      super(0);
    }

    public float getEstimatedTotalCost() {
      return getValue();
    }
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
