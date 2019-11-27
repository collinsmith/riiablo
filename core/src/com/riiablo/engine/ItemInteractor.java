package com.riiablo.engine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.riiablo.Riiablo;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.ItemComponent;

public class ItemInteractor implements InteractableComponent.Interactor {
  private static final String TAG = "ItemInteractor";

  private final ComponentMapper<ItemComponent> itemComponent = ComponentMapper.getFor(ItemComponent.class);

  @Override
  public void interact(Entity src, Entity entity) {
    ItemComponent itemComponent = this.itemComponent.get(entity);
    Riiablo.cursor.setItem(itemComponent.item);
    Riiablo.engine.removeEntity(entity);
  }
}
