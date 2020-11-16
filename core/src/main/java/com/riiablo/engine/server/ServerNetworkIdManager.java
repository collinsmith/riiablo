package com.riiablo.engine.server;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.riiablo.engine.server.component.Networked;

@All(Networked.class)
public class ServerNetworkIdManager extends BaseEntitySystem {
  protected ComponentMapper<Networked> mNetworked;

  @Override
  protected void processSystem() {}

  @Override
  protected void inserted(int entityId) {
    mNetworked.get(entityId).serverId = entityId;
  }
}
