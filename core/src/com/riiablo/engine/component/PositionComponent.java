package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class PositionComponent implements Component, Pool.Poolable {
  public final Vector2 position = new Vector2();

  @Override
  public void reset() {
    position.setZero();
  }
}
