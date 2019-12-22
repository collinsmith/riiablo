package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;

import net.mostlyoriginal.api.event.common.Event;

public class NpcInteractionEvent implements Event {
  @EntityId
  public int entityId;

  @EntityId
  public int npcId;

  public static NpcInteractionEvent obatin(int entityId, int npcId) {
    NpcInteractionEvent event = new NpcInteractionEvent();
    event.entityId = entityId;
    event.npcId = npcId;
    return event;
  }
}
