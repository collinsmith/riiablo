package com.riiablo.engine.server;

import com.riiablo.engine.server.component.Interactable;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ItemInteractor extends PassiveSystem implements Interactable.Interactor {
  private static final String TAG = "ItemInteractor";

  protected ItemManager items;

  @Override
  public void interact(int src, int entity) {
    items.pickupToCursor(src, entity);
    world.delete(entity);
  }
}
