package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.riiablo.Riiablo;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.Item;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ItemInteractor extends PassiveSystem implements Interactable.Interactor {
  private static final String TAG = "ItemInteractor";

  protected ComponentMapper<Item> mItem;

  @Override
  public void interact(int src, int entity) {
    Item item = mItem.get(entity);
    Riiablo.cursor.setItem(item.item);
    world.delete(entity);
  }
}
