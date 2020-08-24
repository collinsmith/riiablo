package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class SkillDoEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;
  public int srvdofunc;
  public int cltdofunc;

  public static SkillDoEvent obtain(int entityId, int skillId, int srvdofunc, int cltdofunc) {
    SkillDoEvent event = new SkillDoEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    event.srvdofunc = srvdofunc;
    event.cltdofunc = cltdofunc;
    return event;
  }
}
