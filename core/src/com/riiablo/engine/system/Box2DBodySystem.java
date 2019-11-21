package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.VelocityComponent;

public class Box2DBodySystem extends IteratingSystem {
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<Box2DComponent> box2DComponent = ComponentMapper.getFor(Box2DComponent.class);

  public Box2DBodySystem() {
    super(Family.all(VelocityComponent.class, Box2DComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    VelocityComponent velocityComponent = this.velocityComponent.get(entity);
    Box2DComponent box2DComponent = this.box2DComponent.get(entity);
    box2DComponent.body.setLinearVelocity(velocityComponent.velocity);

    // FIXME: This is a temp fix to prevent shoving NPCs -- need to explore Contact Filters
    if (velocityComponent.velocity.isZero()) {
      box2DComponent.body.setType(BodyDef.BodyType.StaticBody);
    } else {
      box2DComponent.body.setType(BodyDef.BodyType.DynamicBody);
    }
  }
}
