package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.badlogic.gdx.physics.box2d.Body;
import com.riiablo.engine.EntitySystemAdapter;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.map.Box2DPhysics;

@All(Box2DBody.class)
public class Box2DDisposer extends EntitySystemAdapter {
  protected ComponentMapper<Box2DBody> mBox2DBody;

  protected Box2DPhysics box2d;

  @Override
  protected void removed(int entityId) {
    if (!mBox2DBody.has(entityId)) return;
    Body body = mBox2DBody.get(entityId).body;
    if (body != null) box2d.getPhysics().destroyBody(body);
  }
}
