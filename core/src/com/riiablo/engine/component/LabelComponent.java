package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;

public class LabelComponent implements Component, Pool.Poolable {
  public Actor actor;
  public final Vector2 offset = new Vector2();

  @Override
  public void reset() {
    actor = null;
    offset.setZero();
  }
}
