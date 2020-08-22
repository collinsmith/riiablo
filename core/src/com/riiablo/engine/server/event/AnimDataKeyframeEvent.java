package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class AnimDataKeyframeEvent implements Event {
  @EntityId
  public int entityId;

  public byte keyframe;

  public static AnimDataKeyframeEvent obtain(int entityId, byte keyframe) {
    AnimDataKeyframeEvent event = new AnimDataKeyframeEvent();
    event.entityId = entityId;
    event.keyframe = keyframe;
    return event;
  }
}
