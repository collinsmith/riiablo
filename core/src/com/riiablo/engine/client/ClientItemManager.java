package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.component.Item;
import com.riiablo.engine.server.component.Position;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.StoreLoc;
import com.riiablo.save.ItemController;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class ClientItemManager extends PassiveSystem implements ItemController {
  private static final String TAG = "ClientItemManager";

  protected ComponentMapper<Item> mItem;
  protected ComponentMapper<Position> mPosition;

  @Wire(name = "factory")
  protected EntityFactory factory;

  @Override
  public void groundToCursor(int entityId) {
    com.riiablo.item.Item item = mItem.get(entityId).item;
    Riiablo.charData.groundToCursor(item);

    world.delete(entityId);
  }

  @Override
  public void cursorToGround() {
    com.riiablo.item.Item item = Riiablo.charData.getItems().getCursor();
    Riiablo.charData.cursorToGround();

    Vector2 position = mPosition.get(Riiablo.game.player).position;
    factory.createItem(item, position);
  }

  @Override
  public void storeToCursor(int i) {
    Riiablo.charData.storeToCursor(i);
  }

  @Override
  public void cursorToStore(StoreLoc storeLoc, int x, int y) {
    Riiablo.charData.cursorToStore(storeLoc, x, y);
  }

  @Override
  public void swapStoreItem(int i, StoreLoc storeLoc, int x, int y) {
    Riiablo.charData.swapStoreItem(i, storeLoc, x, y);
  }

  @Override
  public void bodyToCursor(BodyLoc bodyLoc, boolean merc) {
    Riiablo.charData.bodyToCursor(bodyLoc, merc);
  }

  @Override
  public void cursorToBody(BodyLoc bodyLoc, boolean merc) {
    Riiablo.charData.cursorToBody(bodyLoc, merc);
  }

  @Override
  public void swapBodyItem(BodyLoc bodyLoc, boolean merc) {
    Riiablo.charData.swapBodyItem(bodyLoc, merc);
  }

  @Override
  public void beltToCursor(int i) {
    Riiablo.charData.beltToCursor(i);
  }

  @Override
  public void cursorToBelt(int x, int y) {
    Riiablo.charData.cursorToBelt(x, y);
  }

  @Override
  public void swapBeltItem(int i) {
    Riiablo.charData.swapBeltItem(i);
  }
}
