package com.riiablo.item;

import com.badlogic.gdx.Gdx;

public enum Location {
  STORED,
  EQUIPPED,
  BELT,
  UNK3,
  CURSOR,
  UNK5,
  SOCKET;

  public static Location valueOf(int i) {
    switch (i) {
      case 0: return STORED;
      case 1: return EQUIPPED;
      case 2: return BELT;
      case 3: return UNK3;
      case 4: return CURSOR;
      case 5: return UNK5;
      case 6: return SOCKET;
      default:
        Gdx.app.error("Location", "Unknown location: " + i);
        return null;
    }
  }
}
