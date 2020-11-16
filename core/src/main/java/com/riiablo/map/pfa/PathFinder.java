package com.riiablo.map.pfa;

import com.badlogic.gdx.ai.pfa.GraphPath;

public interface PathFinder {
  boolean search(Point2 startNode, Point2 endNode, int flags, int size, GraphPath<Point2> outPath);
}
