package com.riiablo.engine.server;

import org.apache.logging.log4j.Logger;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Casting;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.event.AnimDataFinishedEvent;
import com.riiablo.engine.server.event.AnimDataKeyframeEvent;
import com.riiablo.engine.server.event.SkillCastEvent;
import com.riiablo.log.LogManager;

public class Actioneer extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(Actioneer.class);

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Sequence> mSequence;
  protected ComponentMapper<MovementModes> mMovementModes;
  protected ComponentMapper<Casting> mCasting;

  protected EventSystem events;

  public void cast(int entityId, int skillId) {
    if (mSequence.has(entityId)) return; // TODO: mCasting.has(entityId) ?
    final Skills.Entry skill = Riiablo.files.skills.get(skillId);
    log.trace("casting skill: {}", skill);

    final Class.Type type = mClass.get(entityId).type;
    byte mode = (byte) type.getMode(skill.anim);
    log.trace("mode: {}", mode);
    if (mode == -1) { // TODO: replace -1 with some const INVALID_SKILL
      mode = (byte) type.getMode("SC");
      log.trace("mode changed to {} because it was invalid", mode);
    }

    mSequence.create(entityId).sequence(mode, mMovementModes.get(entityId).NU);
    mCasting.create(entityId).skillId = skillId;
    events.dispatch(SkillCastEvent.obtain(entityId, skillId));

    srvstfunc(entityId, skill.srvstfunc);
  }

  @Subscribe
  public void onKeyframe(AnimDataKeyframeEvent event) {
    if (!mCasting.has(event.entityId)) return;
    log.debug("onKeyframe: {}, {} ({})", event.entityId, event.keyframe, Engine.getKeyframe(event.keyframe));
    final int skillId = mCasting.get(event.entityId).skillId;
    final Skills.Entry skill = Riiablo.files.skills.get(skillId);
    srvdofunc(event.entityId, skill.srvdofunc);
  }

  @Subscribe
  public void onFinished(AnimDataFinishedEvent event) {
    if (!mCasting.has(event.entityId)) return;
    log.debug("onFinished: {}, {} ({})", event.entityId);
    mCasting.remove(event.entityId);
  }

  // start func
  private void srvstfunc(int entityId, int srvstfunc) {
    log.trace("srvstfunc({},{})", entityId, srvstfunc);
    switch (srvstfunc) {
      case 0:
        break;
      case 1: // attack
        break;
      default:
        log.warn("Unsupported srvstfunc({}) for {}", srvstfunc, entityId);
        // TODO: default case will log an error when all valid cases are enumerated
        // log.error("Invalid srvdofunc({}) for {}", srvstfunc, entityId);
    }
  }

  // do func
  private void srvdofunc(int entityId, int srvdofunc) {
    log.trace("srvdofunc({},{})", entityId, srvdofunc);
    switch (srvdofunc) {
      case 0:
        break;
      case 1: // attack
        break;
      default:
        log.warn("Unsupported srvdofunc({}) for {}", srvdofunc, entityId);
        // TODO: default case will log an error when all valid cases are enumerated
        //log.error("Invalid srvdofunc({}) for {}", srvdofunc, entityId);
    }
  }
}
