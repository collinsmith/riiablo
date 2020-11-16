package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.codec.Animation;
import com.riiablo.engine.Direction;

@All({Angle.class, AnimationWrapper.class})
public class DirectionResolver extends IteratingSystem {
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;

  @Override
  protected void process(int entityId) {
    float radians = mAngle.get(entityId).angle.angleRad();
    Animation animation = mAnimationWrapper.get(entityId).animation;
    int d = Direction.radiansToDirection(radians, animation.getNumDirections());
    animation.setDirection(d);
  }
}
