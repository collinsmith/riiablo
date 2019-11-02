package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.IdComponent;

public class IdSystem extends EntitySystem implements EntityListener {
  private final Family family = Family.all(IdComponent.class).get();
  private final ComponentMapper<IdComponent> ids = ComponentMapper.getFor(IdComponent.class);
  private final IntMap<Entity> entitiesById = new IntMap<>();
  private int nextEntityId = IdComponent.INVALID_ID;

  public IdSystem() {
    super(SystemPriority.IdSystem);
    setProcessing(false);
  }

  public Entity getEntity(int id) {
    return entitiesById.get(id);
  }

  private int obtainEntityId() {
    return nextEntityId++;
  }

  @Override
  public void addedToEngine(Engine engine) {
    engine.addEntityListener(family, 0, this);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    engine.removeEntityListener(this);
  }

  @Override
  public void entityAdded(Entity entity) {
    IdComponent c = ids.get(entity);
    if (c.id == IdComponent.INVALID_ID) {
      c.id = obtainEntityId();
    }

    entitiesById.put(c.id, entity);
  }

  @Override
  public void entityRemoved(Entity entity) {
    IdComponent c = ids.get(entity);
    if (c.id != IdComponent.INVALID_ID) {
      entitiesById.remove(c.id);
    }
  }
}
