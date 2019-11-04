package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.DS1;

public class PathComponent implements Component, Pool.Poolable {
  public DS1.Path path;

  @Override
  public void reset() {
    path = null;
  }
}
