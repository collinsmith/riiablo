package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.client.component.AnimationWrapper;

@All(AnimationWrapper.class)
public class AnimationStepper extends IntervalIteratingSystem {
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;

  public AnimationStepper() {
    super(null, Animation.FRAME_DURATION);
  }

  @Override
  protected void process(int entityId) {
    //mAnimationWrapper.get(entityId).animation.update(world.delta);
    mAnimationWrapper.get(entityId).animation.update(Animation.FRAME_DURATION);
  }
}
