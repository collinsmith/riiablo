package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Velocity;

@All({Position.class, Velocity.class})
public class VelocityAdder extends IteratingSystem {
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;

  @Override
  protected void process(int entityId) {
    mPosition.get(entityId).position.mulAdd(mVelocity.get(entityId).velocity, world.delta);
  }
}
