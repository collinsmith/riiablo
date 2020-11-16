package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import com.riiablo.map.Map;

import net.mostlyoriginal.api.event.common.Event;

public class ZoneChangeEvent implements Event {
  @EntityId
  public int entityId;
  public Map.Zone zone;

  public static ZoneChangeEvent obtain(int entityId, Map.Zone zone) {
    ZoneChangeEvent event = new ZoneChangeEvent();
    event.entityId = entityId;
    event.zone = zone;
    return event;
  }
}
