package com.riiablo.engine.server.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import com.riiablo.engine.Engine;

@PooledWeaver
public class Target extends Component {
  // assert target has Position AND Interactable
  @EntityId public int target = Engine.INVALID_ENTITY;
}
