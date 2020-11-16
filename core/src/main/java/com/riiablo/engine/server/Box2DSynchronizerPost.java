package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Position;

@All({Box2DBody.class, Position.class})
public class Box2DSynchronizerPost extends IteratingSystem {
  protected ComponentMapper<Box2DBody> mBox2DBody;
  protected ComponentMapper<Position> mPosition;

  @Override
  protected void process(int entityId) {
    Body body = mBox2DBody.get(entityId).body;
    mPosition.get(entityId).position.set(body.getPosition());
  }
}
