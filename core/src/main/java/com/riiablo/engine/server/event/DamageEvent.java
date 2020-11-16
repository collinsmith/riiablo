package com.riiablo.engine.server.event;

import com.artemis.annotations.EntityId;
import net.mostlyoriginal.api.event.common.Event;

public class DamageEvent implements Event {
  @EntityId
  public int attacker;
  @EntityId
  public int victim;
  public float damage;

  public static DamageEvent obtain(int attacker, int victim, float damage) {
    DamageEvent event = new DamageEvent();
    event.attacker = attacker;
    event.victim = victim;
    event.damage = damage;
    return event;
  }
}
