package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class AngleComponent implements Component, Pool.Poolable {
  public final Vector2 angle  = Vector2.X.cpy().rotateRad(MathUtils.PI / 2);
  public final Vector2 target = angle.cpy();

  @Override
  public void reset() {
    angle.rotateRad(MathUtils.PI / 2);
    target.set(angle);
  }
}
