package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;

public class PhysicsSystem extends IteratingSystem {
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);

  public PhysicsSystem() {
    super(Family.all(PositionComponent.class, VelocityComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    VelocityComponent velocityComponent = this.velocityComponent.get(entity);
    Vector2 velocity = velocityComponent.velocity;
    if (velocity.isZero()) return;
    PositionComponent positionComponent = this.positionComponent.get(entity);
    Vector2 position = positionComponent.position;
    position.mulAdd(velocity, delta);
  }
}
