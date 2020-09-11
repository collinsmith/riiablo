package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.event.DamageEvent;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class DamageHandler extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(DamageHandler.class);

  protected ComponentMapper<AIWrapper> mAIWrapper;

  @Subscribe
  public void onDamageEvent(DamageEvent event) {
    log.traceEntry("onDamageEvent(attacker: {}, victim: {}, damage: {})", event.attacker, event.victim, event.damage);
    mAIWrapper.get(event.victim).ai.hit();
  }
}
