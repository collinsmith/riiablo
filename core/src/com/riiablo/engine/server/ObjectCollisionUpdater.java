package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.Object;

@All({Object.class, Box2DBody.class, CofReference.class})
public class ObjectCollisionUpdater extends IteratingSystem {
  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<Box2DBody> mBox2DBody;
  protected ComponentMapper<CofReference> mCofReference;

// FIXME: doesn't work :/
//  @Subscribe
//  public void onModeChanged(ModeChangeEvent event) {
//    Object object = mObject.get(event.entityId);
//    if (object == null) return;
//    setCollision(event.entityId, object.base.HasCollision[event.mode]);
//  }

  public void setCollision(int entityId, boolean b) {
    Box2DBody bodyWrapper = mBox2DBody.get(entityId);
//    if (bodyWrapper == null || bodyWrapper.body == null) return;
    bodyWrapper.body.setActive(b);
  }

  @Override
  protected void process(int entityId) {
    CofReference reference = mCofReference.get(entityId);
    Object object = mObject.get(entityId);
    setCollision(entityId, object.base.HasCollision[reference.mode]);
  }
}
