package com.riiablo.engine.client.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.codec.Animation;

@Transient
@PooledWeaver
public class AnimationWrapper extends PooledComponent {
  public final Animation animation = Animation.newAnimation();

  @Override
  protected void reset() {
    animation.reset();
  }
}
