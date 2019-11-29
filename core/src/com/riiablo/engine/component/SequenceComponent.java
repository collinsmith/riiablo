package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class SequenceComponent implements Component, Pool.Poolable {
  public int mode1 = CofComponent.MODE_NULL;
  public int mode2 = CofComponent.MODE_NULL;
  public boolean set = false;

  @Override
  public void reset() {
    mode1 = CofComponent.MODE_NULL;
    mode2 = CofComponent.MODE_NULL;
    set = false;
  }
}
