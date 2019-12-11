package com.riiablo.engine.server.event;

public class WClassChangeEvent extends CofChangeEvent {
  public byte wclass;

  public static WClassChangeEvent obtain(int entityId, byte wclass) {
    WClassChangeEvent event = new WClassChangeEvent();
    event.entityId = entityId;
    event.wclass = wclass;
    return event;
  }
}
