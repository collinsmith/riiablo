package com.riiablo.engine.server;

import com.artemis.BaseEntitySystem;
import com.artemis.annotations.All;
import com.riiablo.engine.server.component.Networked;

@All(Networked.class)
public class NetworkSynchronizer extends BaseEntitySystem {
  @Override
  protected void processSystem() {}
}
