package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

import com.badlogic.gdx.math.Vector2;

public class SkillStartEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;
  @EntityId
  public int targetId;
  public Vector2 targetVec;
  public int srvstfunc;
  public int cltstfunc;

  public static SkillStartEvent obtain(
      int entityId, int skillId, int targetId, Vector2 targetVec, int srvstfunc, int cltstfunc) {
    SkillStartEvent event = new SkillStartEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    event.targetId = targetId;
    event.targetVec = targetVec;
    event.srvstfunc = srvstfunc;
    event.cltstfunc = cltstfunc;
    return event;
  }
}
