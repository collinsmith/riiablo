package com.riiablo.engine.client;

import com.riiablo.Riiablo;
import com.riiablo.engine.server.ItemManager;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.StoreLoc;
import com.riiablo.save.ItemController;

import net.mostlyoriginal.api.system.core.PassiveSystem;

// sends item events
// receives item events and applies changes
public class ClientItemManager extends PassiveSystem implements ItemController {
  private static final String TAG = "ClientItemManager";

  protected ItemManager items;

  @Override
  public void groundToCursor(Item item) {
    Riiablo.charData.groundToCursor(item);
  }

  @Override
  public void cursorToGround() {
    Riiablo.charData.cursorToGround();
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
