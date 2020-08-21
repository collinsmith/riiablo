package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class SkillCastEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;

  public static SkillCastEvent obtain(int entityId, int skillId) {
    SkillCastEvent event = new SkillCastEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    return event;
  }
}
