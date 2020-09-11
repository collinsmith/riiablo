package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.event.DeathEvent;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class DeathHandler extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(DeathHandler.class);

  protected ComponentMapper<Sequence> mSequence;

  @Subscribe
  public void onDeathEvent(DeathEvent event) {
    log.traceEntry("onDeathEvent(killer: {}, victim: {})", event.killer, event.victim);
    mSequence.create(event.victim).sequence(Engine.Monster.MODE_DT, Engine.Monster.MODE_DD);
  }
}
