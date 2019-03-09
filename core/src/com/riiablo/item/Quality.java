package com.riiablo.item;

import com.badlogic.gdx.Gdx;

public enum Quality {
  NONE,
  LOW,
  NORMAL,
  HIGH,
  MAGIC,
  SET,
  RARE,
  UNIQUE,
  CRAFTED;

  public static Quality valueOf(int i) {
    switch (i) {
      case 0: return NONE;
      case 1: return LOW;
      case 2: return NORMAL;
      case 3: return HIGH;
      case 4: return MAGIC;
      case 5: return SET;
      case 6: return RARE;
      case 7: return UNIQUE;
      case 8: return CRAFTED;
      default:
        Gdx.app.error("Quality", "Unknown quality: " + i);
        return null;
    }
  }
}
