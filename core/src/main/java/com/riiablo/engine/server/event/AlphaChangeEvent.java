package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;

import net.mostlyoriginal.api.event.common.Event;

public class AlphaChangeEvent implements Event {
  @EntityId public int entityId;
  public int flags;

  public static AlphaChangeEvent obtain(int entityId, int flags) {
    AlphaChangeEvent event = new AlphaChangeEvent();
    event.entityId = entityId;
    event.flags = flags;
    return event;
  }
}
