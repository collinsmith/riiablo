package com.riiablo.engine.server;

import com.badlogic.gdx.math.Vector2;
import com.riiablo.save.CharData;

public class ServerItemManager extends ItemManager {
  private static final String TAG = "ServerItemManager";

  @Override
  public void groundToCursor(int entityId, int dst) {
    super.groundToCursor(entityId, dst);
    world.delete(dst);
  }

  @Override
  public void cursorToGround(int entityId) {
    CharData charData = mPlayer.get(entityId).data;
    com.riiablo.item.Item item = charData.getItems().getCursor();
    super.cursorToGround(entityId);

    Vector2 position = mPosition.get(entityId).position;
    factory.createItem(item, position);
  }
}
