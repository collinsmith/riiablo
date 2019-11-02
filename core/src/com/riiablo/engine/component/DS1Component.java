package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.DS1;

public class DS1Component implements Component, Pool.Poolable {
  public DS1        ds1;
  public DS1.Object object;

  @Override
  public void reset() {
    ds1 = null;
    object = null;
  }
}
