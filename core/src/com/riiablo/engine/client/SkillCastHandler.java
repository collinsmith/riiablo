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
  public void srvstfunc(SkillStartEvent event) {
    log.trace("srvstfunc({},{})", event.entityId, event.srvstfunc);
    switch (event.srvstfunc) {
      case 0:
        break;
      default:
        log.warn("Unsupported srvstfunc({}) for {} casting {}", event.srvstfunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }

  @Subscribe
  public void srvdofunc(SkillDoEvent event) {
    log.trace("srvdofunc({},{})", event.entityId, event.srvdofunc);
    switch (event.srvdofunc) {
      case 0:
        break;
      case 68: // shouts
        Riiablo.audio.play("barbarian_circle_1", true);
        break;
      default:
        log.warn("Unsupported srvdofunc({}) for {} casting {}", event.srvdofunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }

  @Subscribe
  public void cltstfunc(SkillStartEvent event) {
    log.trace("cltstfunc({},{})", event.entityId, event.cltstfunc);
    final Skills.Entry skill = Riiablo.files.skills.get(event.skillId);
    Riiablo.audio.play(skill.stsound, true);

//    // get event.entityId charclass (component does not exist yet) and compare
//    if (!skill.charclass.isEmpty()) {
//      if (CharacterClass.get(skill.charclass) == )
//      Riiablo.audio.play(skill.stsoundclass, true);
//    }

    if (!skill.castoverlay.isEmpty()) {
      overlays.set(event.entityId, skill.castoverlay);
    }

    switch (event.cltstfunc) {
      case 0:
        break;
      default:
        log.warn("Unsupported cltstfunc({}) for {} casting {}", event.cltstfunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }

  @Subscribe
  public void cltdofunc(SkillDoEvent event) {
    log.trace("cltdofunc({},{})", event.entityId, event.cltdofunc);
    final Skills.Entry skill = Riiablo.files.skills.get(event.skillId);
    Riiablo.audio.play(skill.dosound, true);

    switch (event.cltdofunc) {
      case 0:
        break;
      case 25: // shouts / novas
        break;
      default:
        log.warn("Unsupported cltdofunc({}) for {} casting {}", event.cltdofunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }
}
