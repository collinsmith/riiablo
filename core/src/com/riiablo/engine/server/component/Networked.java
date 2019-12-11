package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.riiablo.engine.Engine;

public class Networked extends Component {
  @EntityId
  public int serverId = Engine.INVALID_ENTITY;
}
