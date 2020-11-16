package com.riiablo.map.pfa;

import com.badlogic.gdx.utils.Array;
import com.riiablo.map.MapGraph;

public class JPSPathFinder extends AStarPathFinder {
  final MapGraph graph;
  private final Array<Point2> neighbors = new Array<>(false, 8);

  public JPSPathFinder(MapGraph graph) {
    super(graph);
    this.graph = graph;
  }

  @Override
  protected void visitChildren(Point2 startNode, Point2 endNode, int flags, int size) {
    identifySuccessors(startNode, endNode, flags, size);
  }

  protected void identifySuccessors(Point2 startNode, Point2 endNode, int flags, int size) {
    Array<Point2> neighbors = getPrunedNeighbors(startNode, flags, this.neighbors);
    for (Point2 neighbor : neighbors) {
      Point2 jump = jump(startNode, neighbor, flags, endNode);
      if (jump != null) {
        float g = startNode.g() + heuristic.estimate(startNode, jump);

        float h;
        reset(jump);
        switch (jump.category) {
          case Point2.UNVISITED:
            h = heuristic.estimate(jump, endNode);
            break;
          case Point2.OPEN:
            if (jump.g() <= g) continue;
            openList.remove(jump);
            h = jump.f() - jump.g();
            break;
          case Point2.CLOSED:
            if (jump.g() <= g) continue;
            h = jump.f() - jump.g();
            break;
          default:
            throw new AssertionError("Invalid nodeRecord category: " + jump.category);
        }

        jump.g = g;
        jump.parent = startNode;

        addToOpenList(jump, g + h);
      }
    }
  }

  private Array<Point2> getPrunedNeighbors(Point2 startNode, int flags, Array<Point2> neighbors) {
    Point2 parent = startNode.parent;
    if (parent == null) {
      return graph.getNeighbors(startNode, flags, neighbors);
    } else {
      neighbors.clear();
      final int x = startNode.x;
      final int y = startNode.y;
      final int dx = (x - parent.x) / Math.max(Math.abs(x - parent.x), 1);
      final int dy = (y - parent.y) / Math.max(Math.abs(y - parent.y), 1);
      if (dx != 0 && dy != 0) {
        boolean b1 = graph.tryNeighbor(neighbors, flags, x, y + dy);
        boolean b2 = graph.tryNeighbor(neighbors, flags, x + dx, y);
        if (b1 || b2) neighbors.add(graph.getOrCreate(x + dx, y + dy));
        if (b1 && !graph.isWalkable(x - dx, y, flags)) neighbors.add(graph.getOrCreate(x - dx, y + dy));
        if (b2 && !graph.isWalkable(x, y - dy, flags)) neighbors.add(graph.getOrCreate(x + dx, y - dy));
      } else if (dx == 0) {
        boolean b1 = graph.tryNeighbor(neighbors, flags, x, y + dy);
        if (b1) {
          if (!graph.isWalkable(x + 1, y, flags)) neighbors.add(graph.getOrCreate(x + 1, y + dy));
          if (!graph.isWalkable(x - 1, y, flags)) neighbors.add(graph.getOrCreate(x - 1, y + dy));
        }
      } else {
        boolean b2 = graph.tryNeighbor(neighbors, flags, x + dx, y);
        if (b2) {
          if (!graph.isWalkable(x, y + 1, flags)) neighbors.add(graph.getOrCreate(x + dx, y + 1));
          if (!graph.isWalkable(x, y - 1, flags)) neighbors.add(graph.getOrCreate(x + dx, y - 1));
        }
      }
    }

    return neighbors;
  }

  private Point2 jump(Point2 startNode, Point2 node, int flags, Point2 endNode) {
    final int x = node.x;
    final int y = node.y;
    if (!graph.isWalkable(x, y, flags)) return null;
    if (node == endNode) return endNode;
    final int dx = x - startNode.x;
    final int dy = y - startNode.y;
    if (dx != 0 && dy != 0) {
      if ((graph.isWalkable(x - dx, y + dy, flags) && !graph.isWalkable(x - dx, y, flags))
       || (graph.isWalkable(x + dx, y - dy, flags) && !graph.isWalkable(x, y - dy, flags))) {
        return node;
      }
    } else if (dx != 0) {
      if ((graph.isWalkable(x + dx, y + 1, flags) && !graph.isWalkable(x, y + 1, flags))
       || (graph.isWalkable(x + dx, y - 1, flags) && !graph.isWalkable(x, y - 1, flags))) {
        return node;
      }
    } else {
      if ((graph.isWalkable(x + 1, y + dy, flags) && !graph.isWalkable(x + 1, y, flags))
       || (graph.isWalkable(x - 1, y + dy, flags) && !graph.isWalkable(x - 1, y, flags))) {
        return node;
      }
    }

    if (dx != 0 && dy != 0) {
      if (jump(node, graph.getOrCreate(x + dx, y), flags, endNode) != null) return node;
      if (jump(node, graph.getOrCreate(x, y + dy), flags, endNode) != null) return node;
    }

    if (graph.isWalkable(x + dx, y, flags) || graph.isWalkable(x, y + dy, flags)) {
      return jump(node, graph.getOrCreate(x + dx, y + dy), flags, endNode);
    }

    return null;
  }
}
