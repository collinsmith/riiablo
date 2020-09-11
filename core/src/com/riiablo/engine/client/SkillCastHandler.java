package com.riiablo.engine.client;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.server.event.SkillCastEvent;
import com.riiablo.engine.server.event.SkillDoEvent;
import com.riiablo.engine.server.event.SkillStartEvent;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class SkillCastHandler extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(SkillCastHandler.class);

  protected OverlayManager overlays;

  @Subscribe
  public void onSkillCast(SkillCastEvent event) {
  }

  private static final ObjectMap<String, String> HITCLASS = new ObjectMap<>();
  static {
    HITCLASS.put("1hss", "weapon_1hs_small_1");
    HITCLASS.put("1hsl", "weapon_1hs_large_1");
    HITCLASS.put("2hss", "weapon_2hs_small_1");
    HITCLASS.put("2hsl", "weapon_2hs_large_1");
  }

  @Subscribe
  public void srvstfunc(SkillStartEvent event) {
    log.traceEntry("srvstfunc(entityId: {}, srvstfunc: {}, targetId: {}, targetVec: {})",
        event.entityId, event.srvstfunc, event.targetId, event.targetVec);
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
    log.traceEntry("srvdofunc(entityId: {}, srvdofunc: {}, targetId: {}, targetVec: {})",
        event.entityId, event.srvdofunc, event.targetId, event.targetVec);
    switch (event.srvdofunc) {
      case 0:
        break;
      case 1: // attack
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
    log.traceEntry("cltstfunc(entityId: {}, cltstfunc: {}, targetId: {}, targetVec: {})",
        event.entityId, event.cltstfunc, event.targetId, event.targetVec);
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
    log.traceEntry("cltdofunc(entityId: {}, cltdofunc: {}, targetId: {}, targetVec: {})",
        event.entityId, event.cltdofunc, event.targetId, event.targetVec);
    final Skills.Entry skill = Riiablo.files.skills.get(event.skillId);
    Riiablo.audio.play(skill.dosound, true);

    switch (event.cltdofunc) {
      case 0:
        break;
      case 1: // attack
        Riiablo.audio.play("weapon_1hs_large_1", true); // TODO: hclass of swung weapon
        break;
      case 25: // shouts / novas
        break;
      default:
        log.warn("Unsupported cltdofunc({}) for {} casting {}", event.cltdofunc, event.entityId, event.skillId);
        // TODO: default case will log an error when all valid cases are enumerated
    }
  }
}
