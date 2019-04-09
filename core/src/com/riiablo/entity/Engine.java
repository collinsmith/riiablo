package com.riiablo.entity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import java.util.Iterator;

public class Engine implements Iterable<Entity> {
  private IntMap<Entity> entitiesById = new IntMap<>();
  private Array<Entity> entities = new Array<>(false, 16);
  private int nextEntityId = 33;

  public Engine() {}

  public Entity getEntity(int id) {
    return entitiesById.get(id);
  }

  private int obtainEntityId() {
    System.out.println("entity id = " + nextEntityId);
    return nextEntityId++;
  }

  @Override
  public Iterator<Entity> iterator() {
    return entities.iterator();
  }

  public void add(Entity entity, int id) {
    entity.uuid = id;
    add(entity);
  }

  public void add(Entity entity) {
    if (entity.uuid == 0) {
      entity.uuid = obtainEntityId();
    }

    entitiesById.put(entity.uuid, entity);
    entities.add(entity);
    System.out.println("adding entity " + entity.classname + " - " + entity.uuid);
  }

  public boolean remove(Entity entity) {
    Entity ent = entitiesById.remove(entity.uuid);
    entities.removeValue(entity, true);
    entity.uuid = 0;
    return ent != null;
  }

  public boolean remove(int id) {
    Entity ent = entitiesById.remove(id);
    Entity entity = getEntity(id);
    entities.removeValue(entity, true);
    entity.uuid = 0;
    return ent != null;
  }
}
