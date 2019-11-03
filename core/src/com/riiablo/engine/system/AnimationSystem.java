package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.Flags;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.AnimationComponent;

public class AnimationSystem extends IteratingSystem {
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);

  public AnimationSystem() {
    super(Family.all(AnimationComponent.class).get(), SystemPriority.AnimationSystem);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    Animation animation = animationComponent.get(entity).animation;
    animation.act(delta);
    animation.setHighlighted((entity.flags & Flags.SELECTED) == Flags.SELECTED);
  }
}
