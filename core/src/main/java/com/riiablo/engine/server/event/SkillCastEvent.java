package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

import com.badlogic.gdx.math.Vector2;

public class SkillCastEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;
  @EntityId
  public int targetId;
  public Vector2 targetVec;

  public static SkillCastEvent obtain(int entityId, int skillId, int targetId, Vector2 targetVec) {
    SkillCastEvent event = new SkillCastEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    event.targetId = targetId;
    event.targetVec = targetVec;
    return event;
  }
}
