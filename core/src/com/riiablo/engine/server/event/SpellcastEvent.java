package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class SpellcastEvent implements Event {
  @EntityId
  public int entityId;
  public int skillId;

  public static SpellcastEvent obtain(int entityId, int skillId) {
    SpellcastEvent event = new SpellcastEvent();
    event.entityId = entityId;
    event.skillId = skillId;
    return event;
  }
}
