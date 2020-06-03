package com.riiablo.engine.server;

import com.artemis.annotations.Wire;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.save.ItemController;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ItemInteractor extends PassiveSystem implements Interactable.Interactor {
  private static final String TAG = "ItemInteractor";

  protected ItemManager items;

  @Wire(name = "itemController", failOnNull = false)
  protected ItemController itemController;

  @Override
  public void interact(int src, int entity) {
    if (itemController != null) {
      itemController.groundToCursor(entity);
    }
  }
}
