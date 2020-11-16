package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.physics.box2d.Body;

import com.riiablo.engine.Engine;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.Target;
import com.riiablo.engine.server.event.DeathEvent;
import com.riiablo.engine.server.event.ModeChangeEvent;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map.Box2DPhysics;

public class DeathHandler extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(DeathHandler.class);

  protected ComponentMapper<AIWrapper> mAIWrapper;
  protected ComponentMapper<Monster> mMonster;
  protected ComponentMapper<Target> mTarget;

  protected CofManager cofs;

  protected ComponentMapper<Box2DBody> mBox2DBody;
  protected Box2DPhysics box2d;

  @Subscribe
  public void onDeathEvent(DeathEvent event) {
    log.traceEntry("onDeathEvent(killer: {}, victim: {})", event.killer, event.victim);
    mAIWrapper.get(event.victim).ai.kill();
    if (mTarget.has(event.killer) && mTarget.get(event.killer).target == event.victim) {
      mTarget.remove(event.killer);
    }
  }

  @Subscribe
  public void onModeChanged(ModeChangeEvent event) {
    final int entityId = event.entityId;
    if (mMonster.has(entityId) && event.mode == Engine.Monster.MODE_DD) {
      if (mBox2DBody.has(entityId)) { // FIXME: should be done somewhere else
        Body body = mBox2DBody.get(entityId).body;
        if (body != null) box2d.getPhysics().destroyBody(body);
      }

      world.delete(entityId);
    }
  }
}
