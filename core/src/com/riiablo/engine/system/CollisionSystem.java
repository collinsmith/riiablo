package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.ObjectComponent;

public class CollisionSystem extends EntitySystem {
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<ObjectComponent> objectComponent = ComponentMapper.getFor(ObjectComponent.class);
  private final ComponentMapper<Box2DComponent> box2DComponent = ComponentMapper.getFor(Box2DComponent.class);
  private final Family objectFamily = Family.all(ObjectComponent.class, CofComponent.class, Box2DComponent.class).get();
  private ImmutableArray<Entity> objectEntities;

  public CollisionSystem() {
    super();
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    objectEntities = engine.getEntitiesFor(objectFamily);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    objectEntities = null;
  }

  @Override
  public void update(float delta) {
    super.update(delta);
    for (Entity entity : objectEntities) {
      CofComponent cofComponent = this.cofComponent.get(entity);
      ObjectComponent objectComponent = this.objectComponent.get(entity);
      setCollision(entity, objectComponent.base.HasCollision[cofComponent.mode]);
    }
  }

  private void setCollision(Entity entity, boolean b) {
    Box2DComponent box2DComponent = this.box2DComponent.get(entity);
    box2DComponent.body.setActive(b);
    // TODO: update map flags
  }
}
