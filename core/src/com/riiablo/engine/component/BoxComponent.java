package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.util.BBox;

public class BoxComponent implements Component, Pool.Poolable {
  public BBox box;

  @Override
  public void reset() {
    box = null;
  }
}
