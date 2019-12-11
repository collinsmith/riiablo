package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.map.pfa.GraphPath;

import java.util.Collections;
import java.util.Iterator;

@Transient
@PooledWeaver
public class Pathfind extends PooledComponent {
  public GraphPath path;
  public Iterator<Vector2> targets = Collections.emptyIterator();
  public final Vector2 target = new Vector2();

  public void reset() {
    path = null;
    target.setZero();
    targets = Collections.emptyIterator();
  }

  public Pathfind set(GraphPath path) {
    this.path = path;
    targets = path.vectorIterator();
    Vector2 position = targets.next();
    target.set(targets.hasNext() ? targets.next() : position);
    return this;
  }
}
