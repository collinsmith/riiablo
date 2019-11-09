package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.COF;
import com.riiablo.engine.Direction;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;

public class AngleSystem extends IteratingSystem {
  private final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);

  public AngleSystem() {
    super(Family.all(AnimationComponent.class, CofComponent.class, AngleComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    AngleComponent angleComponent = this.angleComponent.get(entity);
    float radians = angleComponent.angle;

    CofComponent cofComponent = this.cofComponent.get(entity);
    COF cof = cofComponent.cof;

    int d = Direction.radiansToDirection(radians, cof.getNumDirections());

    AnimationComponent animationComponent = this.animationComponent.get(entity);
    animationComponent.animation.setDirection(d);
  }


}
