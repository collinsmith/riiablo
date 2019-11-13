package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class MovementModeComponent implements Component, Pool.Poolable {
  public byte NU = CofComponent.MODE_NULL;
  public byte WL = CofComponent.MODE_NULL;
  public byte RN = CofComponent.MODE_NULL;

  @Override
  public void reset() {
    NU = CofComponent.MODE_NULL;
    WL = CofComponent.MODE_NULL;
    RN = CofComponent.MODE_NULL;
  }
}
