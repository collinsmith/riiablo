package com.riiablo.engine.client;

import org.apache.logging.log4j.Logger;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.server.event.SkillCastEvent;
import com.riiablo.engine.server.event.SkillDoEvent;
import com.riiablo.engine.server.event.SkillStartEvent;
import com.riiablo.log.LogManager;

public class SkillCastHandler extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(SkillCastHandler.class);

  protected OverlayManager overlays;

  @Subscribe
  public void onSkillCast(SkillCastEvent event) {
  }

  @Subscribe
  public void cltstfunc(SkillStartEvent event) {
    final Skills.Entry skill = Riiablo.files.skills.get(event.skillId);
    Riiablo.audio.play(skill.stsound, true);

    if (!skill.castoverlay.isEmpty()) {
      overlays.set(event.entityId, skill.castoverlay);
    }

    switch (event.cltstfunc) {
      default:
        log.warn("Unsupported cltstfunc({}) for {} casting {}", event.cltstfunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }

  @Subscribe
  public void cltdofunc(SkillDoEvent event) {
    switch (event.cltdofunc) {
      case 0:
        break;
      case 25: // shout
        Riiablo.audio.play("barbarian_circle_1", true);
        break;
      default:
        log.warn("Unsupported cltdofunc({}) for {} casting {}", event.cltdofunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }
}
