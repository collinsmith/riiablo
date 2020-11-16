package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class DeathEvent implements Event {
  @EntityId
  public int killer;
  @EntityId
  public int victim;

  public static DeathEvent obtain(int killer, int victim) {
    DeathEvent event = new DeathEvent();
    event.killer = killer;
    event.victim = victim;
    return event;
  }
}
