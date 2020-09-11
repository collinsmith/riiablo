package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import net.mostlyoriginal.api.event.common.EventSystem;
import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.math.Vector2;

import com.riiablo.Riiablo;
import com.riiablo.attributes.Attributes;
import com.riiablo.attributes.Stat;
import com.riiablo.attributes.StatRef;
import com.riiablo.codec.excel.Skills;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.AttributesWrapper;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Casting;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.event.AnimDataFinishedEvent;
import com.riiablo.engine.server.event.AnimDataKeyframeEvent;
import com.riiablo.engine.server.event.DeathEvent;
import com.riiablo.engine.server.event.SkillCastEvent;
import com.riiablo.engine.server.event.SkillDoEvent;
import com.riiablo.engine.server.event.SkillStartEvent;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.math.Fixed;

public class Actioneer extends PassiveSystem {
  private static final Logger log = LogManager.getLogger(Actioneer.class);

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Sequence> mSequence;
  protected ComponentMapper<MovementModes> mMovementModes;
  protected ComponentMapper<Casting> mCasting;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<AttributesWrapper> mAttributesWrapper;

  // teleport-specific components
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Box2DBody> mBox2DBody;

  protected EventSystem events;

  private boolean canCast(int entityId) {
    if (mCasting.has(entityId)) return false;
    if (mSequence.has(entityId)) return false;
    // TODO: unsure if both checks will be needed -- may be more appropriate to use pflags
    return true;
  }

  public void cast(int entityId, int skillId, int targetId, Vector2 targetVec) {
    if (!canCast(entityId)) return;
    final Skills.Entry skill = Riiablo.files.skills.get(skillId);
    log.traceEntry("cast(entityId: {}, skillId: {} ({}), targetId: {}, targetVec: {})",
        entityId, skillId, skill, targetId, targetVec);

    targetVec = targetVec != null ? targetVec.cpy() : Vector2.Zero;
    final Class.Type type = mClass.get(entityId).type;
    byte mode = (byte) type.getMode(skill.anim);
    log.trace("mode: {}", mode);
    if (mode == Engine.INVALID_MODE) {
      mode = (byte) type.getMode("SC");
      log.trace("mode changed to {} because it was invalid", mode);
    }

    Vector2 entityPos = mPosition.get(entityId).position;
    mAngle.get(entityId).target.set(targetVec).sub(entityPos).nor();
    mSequence.create(entityId).sequence(mode, mMovementModes.get(entityId).NU);
    mCasting.create(entityId).set(skillId, targetId, targetVec);
    events.dispatch(SkillCastEvent.obtain(entityId, skillId, targetId, targetVec));

    srvstfunc(entityId, skill.srvstfunc, targetId, targetVec);
    events.dispatch(SkillStartEvent.obtain(entityId, skillId, targetId, targetVec, skill.srvstfunc, skill.cltstfunc));
  }

  @Subscribe
  public void onAnimDataKeyframe(AnimDataKeyframeEvent event) {
    if (!mCasting.has(event.entityId)) return;
    log.traceEntry("onAnimDataKeyframe(entityId: {}, keyframe: {} ({}))",
        event.entityId, event.keyframe, Engine.getKeyframe(event.keyframe));
    final Casting casting = mCasting.get(event.entityId);
    final Skills.Entry skill = Riiablo.files.skills.get(casting.skillId);
    srvdofunc(event.entityId, skill.srvdofunc, casting.targetId, casting.targetVec);
    events.dispatch(SkillDoEvent.obtain(
        event.entityId, casting.skillId,
        casting.targetId, casting.targetVec,
        skill.srvdofunc, skill.cltdofunc));
  }

  @Subscribe
  public void onAnimDataFinished(AnimDataFinishedEvent event) {
    if (!mCasting.has(event.entityId)) return;
    log.traceEntry("onAnimDataFinished(entityId: {})", event.entityId);
    mCasting.remove(event.entityId);
  }

  private void srvstfunc(int entityId, int srvstfunc, int targetId, Vector2 targetVec) {
    log.traceEntry("srvstfunc(entityId: {}, srvstfunc: {}, targetId: {}, targetVec: {})",
        entityId, srvstfunc, targetId, targetVec);
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

  private void srvdofunc(int entityId, int srvdofunc, int targetId, Vector2 targetVec) {
    log.traceEntry("srvdofunc(entityId: {}, srvdofunc: {}, targetId: {}, targetVec: {})",
        entityId, srvdofunc, targetId, targetVec);
    switch (srvdofunc) {
      case 0:
        break;
      case 1: // attack
        if (targetId == Engine.INVALID_ENTITY) return;
        log.debug("{} attack {}", entityId, targetId);

        Attributes attrs = mAttributesWrapper.get(targetId).attrs;
        StatRef hitpoints = attrs.get(Stat.hitpoints);
        log.debug("{} {}", targetId, hitpoints.asFixed());

        hitpoints.sub(50f);
        log.debug("{} {}", targetId, hitpoints.asFixed());

        if (Fixed.isNegative(hitpoints.encodedValues())) {
          hitpoints.set(0f);
        }

        if (hitpoints.asFixed() <= 0f) {
          log.debug("{} is dead!", targetId);
          events.dispatch(DeathEvent.obtain(entityId, targetId));
        }
        break;
      case 27: // teleport
        mPosition.get(entityId).position.set(targetVec);
        Box2DBody box2dWrapper = mBox2DBody.get(entityId);
        if (box2dWrapper != null) box2dWrapper.body.setTransform(targetVec, 0);
        break;
      default:
        log.warn("Unsupported srvdofunc({}) for {}", srvdofunc, entityId);
        // TODO: default case will log an error when all valid cases are enumerated
        //log.error("Invalid srvdofunc({}) for {}", srvdofunc, entityId);
    }
  }
}
