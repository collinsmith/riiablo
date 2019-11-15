package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class InteractableComponent implements Component, Pool.Poolable {
  public float range;

  @Override
  public void reset() {
    range = 0.0f;
  }
}
