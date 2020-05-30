package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.riiablo.engine.server.component.Item;
import com.riiablo.engine.server.component.Player;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.StoreLoc;

import net.mostlyoriginal.api.system.core.PassiveSystem;

@All(Player.class)
public class ItemManager extends PassiveSystem {
  private static final String TAG = "ItemManager";

  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Item> mItem;

  public void groundToCursor(int entityId, int dst) {
    com.riiablo.item.Item item = mItem.get(dst).item;
    mPlayer.get(entityId).data.groundToCursor(item);
  }

  public void cursorToGround(int entityId) {
    mPlayer.get(entityId).data.cursorToGround();
  }

  public void storeToCursor(int entityId, int i) {
    mPlayer.get(entityId).data.storeToCursor(i);
  }

  public void cursorToStore(int entityId, int storeLoc, int x, int y) {
    StoreLoc s = StoreLoc.valueOf(storeLoc);
    mPlayer.get(entityId).data.cursorToStore(s, x, y);
  }

  public void swapStoreItem(int entityId, int i, int storeLoc, int x, int y) {
    StoreLoc s = StoreLoc.valueOf(storeLoc);
    mPlayer.get(entityId).data.swapStoreItem(i, s, x, y);
  }

  public void bodyToCursor(int entityId, int bodyLoc, boolean merc) {
    BodyLoc b = BodyLoc.valueOf(bodyLoc);
    mPlayer.get(entityId).data.bodyToCursor(b, merc);
  }

  public void cursorToBody(int entityId, int bodyLoc, boolean merc) {
    BodyLoc b = BodyLoc.valueOf(bodyLoc);
    mPlayer.get(entityId).data.cursorToBody(b, merc);
  }

  public void swapBodyItem(int entityId, int bodyLoc, boolean merc) {
    BodyLoc b = BodyLoc.valueOf(bodyLoc);
    mPlayer.get(entityId).data.swapBodyItem(b, merc);
  }

  public void beltToCursor(int entityId, int i) {
    mPlayer.get(entityId).data.beltToCursor(i);
  }

  public void cursorToBelt(int entityId, int x, int y) {
    mPlayer.get(entityId).data.cursorToBelt(x, y);
  }

  public void swapBeltItem(int entityId, int i) {
    mPlayer.get(entityId).data.swapBeltItem(i);
  }
}
