package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import com.riiablo.engine.Engine;

import net.mostlyoriginal.api.event.common.Event;

public class CofChangeEvent implements Event {
  @EntityId public int entityId = Engine.INVALID_ENTITY;
}
