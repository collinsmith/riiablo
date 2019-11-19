package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.PathFinderQueue;
import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.TimeUtils;
import com.riiablo.map.MapGraph;

//refactor of com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
public class IndexedAStarPathFinder<N> implements PathFinder<N> {
  IndexedGraph<N> graph;
  MapGraph casted;
  NodeRecord<N>[] nodeRecords;
  BinaryHeap<NodeRecord<N>> openList;
  NodeRecord<N> current;
  public Metrics metrics;

  private int searchId;

  private static final int UNVISITED = 0;
  private static final int OPEN = 1;
  private static final int CLOSED = 2;

  private int size;

  public IndexedAStarPathFinder<N> setSize(int size) {
    this.size = size;
    return this;
  }

  public IndexedAStarPathFinder(MapGraph graph) {
    this(graph, false);
  }

  @SuppressWarnings("unchecked")
  public IndexedAStarPathFinder (MapGraph graph, boolean calculateMetrics) {
    this.graph = (IndexedGraph<N>) graph;
    this.casted = graph;
    this.nodeRecords = (NodeRecord<N>[])new NodeRecord[graph.getNodeCount()];
    this.openList = new BinaryHeap<>();
    if (calculateMetrics) this.metrics = new Metrics();
  }

  @Override
  public boolean searchConnectionPath(N startNode, N endNode, Heuristic<N> heuristic, GraphPath<Connection<N>> outPath) {
    boolean found = search(startNode, endNode, heuristic);
    if (found) generateConnectionPath(startNode, outPath);
    return found;
  }

  @Override
  public boolean searchNodePath(N startNode, N endNode, Heuristic<N> heuristic, GraphPath<N> outPath) {
    boolean found = search(startNode, endNode, heuristic);
    if (found) generateNodePath(startNode, outPath);
    return found;
  }


  protected boolean search (N startNode, N endNode, Heuristic<N> heuristic) {
    initSearch(startNode, endNode, heuristic);
    do {
      current = openList.pop();
      current.category = CLOSED;
      if (current.node == endNode) return true;
      visitChildren(endNode, heuristic);
    } while (openList.size > 0);
    return false;
  }


  @Override
  public boolean search (PathFinderRequest<N> request, long timeToRun) {
    long lastTime = TimeUtils.nanoTime();
    if (request.statusChanged) {
      initSearch(request.startNode, request.endNode, request.heuristic);
      request.statusChanged = false;
    }

    do {
      long currentTime = TimeUtils.nanoTime();
      timeToRun -= currentTime - lastTime;
      if (timeToRun <= PathFinderQueue.TIME_TOLERANCE) return false;
      current = openList.pop();
      current.category = CLOSED;
      if (current.node == request.endNode) {
        request.pathFound = true;
        generateNodePath(request.startNode, request.resultPath);
        return true;
      }

      visitChildren(request.endNode, request.heuristic);
      lastTime = currentTime;
    } while (openList.size > 0);
    request.pathFound = false;
    return true;
  }

  protected void initSearch (N startNode, N endNode, Heuristic<N> heuristic) {
    if (metrics != null) metrics.reset();
    if (++searchId < 0) searchId = 1;
    openList.clear();

    NodeRecord<N> startRecord = getNodeRecord(startNode);
    startRecord.node = startNode;
    startRecord.connection = null;
    startRecord.costSoFar = 0;
    addToOpenList(startRecord, heuristic.estimate(startNode, endNode));

    current = null;
  }

  protected void visitChildren (N endNode, Heuristic<N> heuristic) {
    Array<Connection<N>> connections = graph.getConnections(current.node);

    for (int i = 0; i < connections.size; i++) {
      if (metrics != null) metrics.visitedNodes++;

      Connection<N> connection = connections.get(i);

      N node = connection.getToNode();
      float nodeCost = current.costSoFar + connection.getCost();

      float nodeHeuristic;
      NodeRecord<N> nodeRecord = getNodeRecord(node);
      if (nodeRecord.category == CLOSED) {
        if (nodeRecord.costSoFar <= nodeCost) continue;
        nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
      } else if (nodeRecord.category == OPEN) {
        if (nodeRecord.costSoFar <= nodeCost) continue;
        openList.remove(nodeRecord);
        nodeHeuristic = nodeRecord.getEstimatedTotalCost() - nodeRecord.costSoFar;
      } else if (casted.getClearance((MapGraph.Point2) node) < size) {
        continue;
      } else {
        nodeHeuristic = heuristic.estimate(node, endNode);
      }

      nodeRecord.costSoFar = nodeCost;
      nodeRecord.connection = connection;

      addToOpenList(nodeRecord, nodeCost + nodeHeuristic);
    }
  }

  protected void generateConnectionPath (N startNode, GraphPath<Connection<N>> outPath) {
    while (current.node != startNode) {
      outPath.add(current.connection);
      current = nodeRecords[graph.getIndex(current.connection.getFromNode())];
    }
    outPath.reverse();
  }

  protected void generateNodePath (N startNode, GraphPath<N> outPath) {
    while (current.connection != null) {
      outPath.add(current.node);
      current = nodeRecords[graph.getIndex(current.connection.getFromNode())];
    }
    outPath.add(startNode);
    outPath.reverse();
  }

  protected void addToOpenList (NodeRecord<N> nodeRecord, float estimatedTotalCost) {
    openList.add(nodeRecord, estimatedTotalCost);
    nodeRecord.category = OPEN;
    if (metrics != null) {
      metrics.openListAdditions++;
      metrics.openListPeak = Math.max(metrics.openListPeak, openList.size);
    }
  }

  protected NodeRecord<N> getNodeRecord (N node) {
    int index = graph.getIndex(node);
    NodeRecord<N> nr = nodeRecords[index];
    if (nr != null) {
      if (nr.searchId != searchId) {
        nr.category = UNVISITED;
        nr.searchId = searchId;
      }
      return nr;
    }
    nr = nodeRecords[index] = new NodeRecord<N>();
    nr.node = node;
    nr.searchId = searchId;
    return nr;
  }

  static class NodeRecord<N> extends BinaryHeap.Node {
    N node;
    Connection<N> connection;
    float costSoFar;
    int category;
    int searchId;

    public NodeRecord () {
      super(0);
    }

    public float getEstimatedTotalCost () {
      return getValue();
    }
  }

  public static class Metrics {
    public int visitedNodes;
    public int openListAdditions;
    public int openListPeak;

    public void reset () {
      visitedNodes = 0;
      openListAdditions = 0;
      openListPeak = 0;
    }
  }
}
