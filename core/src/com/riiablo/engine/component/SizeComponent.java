package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class SizeComponent implements Component, Pool.Poolable {
  public static final int INSIGNIFICANT = 0;
  public static final int SMALL         = 1;
  public static final int MEDIUM        = 2;
  public static final int LARGE         = 3;

  public int size = INSIGNIFICANT;

  @Override
  public void reset() {
    size = INSIGNIFICANT;
  }
}
