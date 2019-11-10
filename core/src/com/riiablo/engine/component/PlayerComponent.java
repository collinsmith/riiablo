package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.CharData;

public class PlayerComponent implements Component, Pool.Poolable {
  public CharData charData = null;

  @Override
  public void reset() {
    charData = null;
  }
}
