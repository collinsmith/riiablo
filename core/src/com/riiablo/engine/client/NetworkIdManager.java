package com.riiablo.engine.client;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Networked;

@All(Networked.class)
public class NetworkIdManager extends BaseEntitySystem {
  final IntIntMap serverToEntityId = new IntIntMap();

  protected ComponentMapper<Networked> mNetworked;

  @Override
  protected void processSystem() {}

  @Override
  protected void inserted(int entityId) {
    serverToEntityId.put(mNetworked.get(entityId).serverId, entityId);
  }

  @Override
  protected void removed(int entityId) {
    serverToEntityId.remove(mNetworked.get(entityId).serverId, Engine.INVALID_ENTITY);
  }
}
