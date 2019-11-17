package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.VelocityComponent;

public class Box2DBodySystem extends IteratingSystem {
  private final ComponentMapper<VelocityComponent> velocitycomponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<Box2DComponent> box2DComponent = ComponentMapper.getFor(Box2DComponent.class);

  public Box2DBodySystem() {
    super(Family.all(VelocityComponent.class, Box2DComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    VelocityComponent velocityComponent = this.velocitycomponent.get(entity);
    Box2DComponent box2DComponent = this.box2DComponent.get(entity);
    box2DComponent.body.setLinearVelocity(velocityComponent.velocity);
  }
}
