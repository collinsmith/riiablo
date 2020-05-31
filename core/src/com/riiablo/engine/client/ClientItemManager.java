package com.riiablo.engine.client;

import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.StoreLoc;
import com.riiablo.save.ItemController;

import net.mostlyoriginal.api.system.core.PassiveSystem;

// sends item events
// receives item events and applies changes
public class ClientItemManager extends PassiveSystem implements ItemController {
  private static final String TAG = "ClientItemManager";

  final ItemController delegate;

  public ClientItemManager(ItemController delegate) {
    this.delegate = delegate;
  }

  @Override
  public void groundToCursor(Item item) {
    delegate.groundToCursor(item);
  }

  @Override
  public void cursorToGround() {
    delegate.cursorToGround();
  }

  @Override
  public void storeToCursor(int i) {
    delegate.storeToCursor(i);
  }

  @Override
  public void cursorToStore(StoreLoc storeLoc, int x, int y) {
    delegate.cursorToStore(storeLoc, x, y);
  }

  @Override
  public void swapStoreItem(int i, StoreLoc storeLoc, int x, int y) {
    delegate.swapStoreItem(i, storeLoc, x, y);
  }

  @Override
  public void bodyToCursor(BodyLoc bodyLoc, boolean merc) {
    delegate.bodyToCursor(bodyLoc, merc);
  }

  @Override
  public void cursorToBody(BodyLoc bodyLoc, boolean merc) {
    delegate.cursorToBody(bodyLoc, merc);
  }

  @Override
  public void swapBodyItem(BodyLoc bodyLoc, boolean merc) {
    delegate.swapBodyItem(bodyLoc, merc);
  }

  @Override
  public void beltToCursor(int i) {
    delegate.beltToCursor(i);
  }

  @Override
  public void cursorToBelt(int x, int y) {
    delegate.cursorToBelt(x, y);
  }

  @Override
  public void swapBeltItem(int i) {
    delegate.swapBeltItem(i);
  }
}
