package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;

import net.mostlyoriginal.api.event.common.Event;

public class AnimDataFinishedEvent implements Event {
  @EntityId
  public int entityId;

  public static AnimDataFinishedEvent obtain(int entityId) {
    AnimDataFinishedEvent event = new AnimDataFinishedEvent();
    event.entityId = entityId;
    return event;
  }
}
