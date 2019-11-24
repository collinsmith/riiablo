package com.riiablo.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;

public class EntityAdapter implements EntityListener {
  @Override public void entityAdded(Entity entity) {}
  @Override  public void entityRemoved(Entity entity) {}
}
