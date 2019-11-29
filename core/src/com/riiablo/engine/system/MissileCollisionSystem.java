package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.engine.component.MissileComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;

// FIXME: this might be temporary until a BOX2D component can be created correctly
public class MissileCollisionSystem extends IteratingSystem {
  private final ComponentMapper<MissileComponent> missileComponent = ComponentMapper.getFor(MissileComponent.class);
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);

  public MissileCollisionSystem() {
    super(Family.all(MissileComponent.class, VelocityComponent.class, PositionComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    MissileComponent missileComponent = this.missileComponent.get(entity);
    PositionComponent positionComponent = this.positionComponent.get(entity);
    VelocityComponent velocityComponent = this.velocityComponent.get(entity);

    Vector2 position = positionComponent.position;
    Vector2 velocity = velocityComponent.velocity;
    position.mulAdd(velocity, delta);
    // TODO: hit detection
    if (missileComponent.start.dst(position) > missileComponent.range) {
      Riiablo.engine.removeEntity(entity);
    }
  }
}
