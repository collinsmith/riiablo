package com.riiablo.engine.server.event;

public class ModeChangeEvent extends CofChangeEvent {
  public byte mode;

  public static ModeChangeEvent obtain(int entityId, byte mode) {
    ModeChangeEvent event = new ModeChangeEvent();
    event.entityId = entityId;
    event.mode = mode;
    return event;
  }
}
