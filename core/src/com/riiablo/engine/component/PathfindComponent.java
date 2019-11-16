package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.MapGraph;

import java.util.Collections;
import java.util.Iterator;

public class PathfindComponent implements Component, Pool.Poolable {
  public MapGraph.MapGraphPath path = null;
  public final Vector2 target = new Vector2();
  public Iterator<MapGraph.Point2> targets = Collections.emptyIterator();

  @Override
  public void reset() {
    path = null;
    target.setZero();
    targets = Collections.emptyIterator();
  }
}
