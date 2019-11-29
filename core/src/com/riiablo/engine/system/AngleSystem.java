package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.Direction;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.AnimationComponent;

public class AngleSystem extends IteratingSystem {
  private final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);

  public AngleSystem() {
    super(Family.all(AnimationComponent.class, AngleComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    AngleComponent angleComponent = this.angleComponent.get(entity);
    float radians = angleComponent.angle.angleRad();

    AnimationComponent animationComponent = this.animationComponent.get(entity);
    Animation animation = animationComponent.animation;
    int d = Direction.radiansToDirection(radians, animation.getNumDirections());
    animation.setDirection(d);
  }
}
