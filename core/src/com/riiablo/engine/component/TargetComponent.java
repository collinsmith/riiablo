package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class TargetComponent implements Component, Pool.Poolable {
  // assert entity has PositionComponent AND InteractableComponent
  public Entity target = null;

  @Override
  public void reset() {
    target = null;
  }
}
