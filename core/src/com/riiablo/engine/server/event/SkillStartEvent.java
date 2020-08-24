package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class SkillStartEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;
  public int srvstfunc;
  public int cltstfunc;

  public static SkillStartEvent obtain(int entityId, int skillId, int srvstfunc, int cltstfunc) {
    SkillStartEvent event = new SkillStartEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    event.srvstfunc = srvstfunc;
    event.cltstfunc = cltstfunc;
    return event;
  }
}
