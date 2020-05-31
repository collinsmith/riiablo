package com.riiablo.save;

import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.StoreLoc;

public interface ItemController {
  void groundToCursor(Item item);
  void cursorToGround();
  void storeToCursor(int i);
  void cursorToStore(StoreLoc storeLoc, int x, int y);
  void swapStoreItem(int i, StoreLoc storeLoc, int x, int y);
  void bodyToCursor(BodyLoc bodyLoc, boolean merc);
  void cursorToBody(BodyLoc bodyLoc, boolean merc);
  void swapBodyItem(BodyLoc bodyLoc, boolean merc);
  void beltToCursor(int i);
  void cursorToBelt(int x, int y);
  void swapBeltItem(int i);
}
