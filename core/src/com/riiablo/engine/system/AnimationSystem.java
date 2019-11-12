package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.HoveredComponent;

public class AnimationSystem extends IteratingSystem {
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<HoveredComponent> hoveredComponent = ComponentMapper.getFor(HoveredComponent.class);

  public AnimationSystem() {
    super(Family.all(AnimationComponent.class).get(), SystemPriority.AnimationSystem);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    Animation animation = animationComponent.get(entity).animation;
    animation.act(delta);
    animation.setHighlighted(hoveredComponent.has(entity));
  }
}
