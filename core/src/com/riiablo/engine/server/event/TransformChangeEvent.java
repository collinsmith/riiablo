package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;

import net.mostlyoriginal.api.event.common.Event;

public class TransformChangeEvent implements Event {
  @EntityId public int entityId;
  public int flags;

  public static TransformChangeEvent obtain(int entityId, int flags) {
    TransformChangeEvent event = new TransformChangeEvent();
    event.entityId = entityId;
    event.flags = flags;
    return event;
  }
}
