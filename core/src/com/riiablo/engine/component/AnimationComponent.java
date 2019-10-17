package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.Animation;

public class AnimationComponent implements Component, Pool.Poolable {
  public Animation animation = Animation.newAnimation();

  @Override
  public void reset() {
    //animation = Animation.newAnimation(); // FIXME: animation.reset();
  }
}
