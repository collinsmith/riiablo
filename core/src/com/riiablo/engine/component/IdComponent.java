package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class IdComponent implements Component, Pool.Poolable {
  public static final int INVALID_ID = 0;

  public int id = INVALID_ID;

  @Override
  public void reset() {
    id = INVALID_ID;
  }
}
