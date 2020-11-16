package com.riiablo.engine.client.event;

import net.mostlyoriginal.api.event.common.Event;

public class InteractEvent implements Event {
  public static InteractEvent obtain() {
    InteractEvent event = new InteractEvent();
    return event;
  }
}
