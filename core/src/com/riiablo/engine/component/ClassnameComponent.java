package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ClassnameComponent implements Component, Pool.Poolable {
  public String classname = null;

  @Override
  public void reset() {
    classname = null;
  }
}
