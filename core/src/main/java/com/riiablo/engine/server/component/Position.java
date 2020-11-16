package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.math.Vector2;

@PooledWeaver
public class Position extends PooledComponent {
  public final Vector2 position = new Vector2();

  @Override
  protected void reset() {
    position.setZero();
  }
}
