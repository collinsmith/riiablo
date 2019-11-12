package com.riiablo.engine.component.cof;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.engine.Dirty;

public class TransformUpdate implements Component, Pool.Poolable {
  public int flags = Dirty.NONE;

  @Override
  public void reset() {
    flags = Dirty.NONE;
  }
}
