package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

import com.badlogic.gdx.math.Vector2;

public class SkillDoEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;
  @EntityId
  public int targetId;
  public Vector2 targetVec;
  public int srvdofunc;
  public int cltdofunc;

  public static SkillDoEvent obtain(
      int entityId, int skillId, int targetId, Vector2 targetVec, int srvdofunc, int cltdofunc) {
    SkillDoEvent event = new SkillDoEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    event.targetId = targetId;
    event.targetVec = targetVec;
    event.srvdofunc = srvdofunc;
    event.cltdofunc = cltdofunc;
    return event;
  }
}
