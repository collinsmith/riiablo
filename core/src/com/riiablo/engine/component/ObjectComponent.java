package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.excel.Objects;

public class ObjectComponent implements Component, Pool.Poolable {
  public Objects.Entry base;

  @Override
  public void reset() {
    base = null;
  }
}
