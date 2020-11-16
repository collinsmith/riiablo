package com.riiablo.item;

import com.badlogic.gdx.Gdx;

public enum StoreLoc {
  NONE,
  INVENTORY,
  UNK2,
  UNK3,
  CUBE,
  STASH;

  public static StoreLoc valueOf(int i) {
    switch (i) {
      case 0:  return NONE;
      case 1:  return INVENTORY;
      case 2:  return UNK2;
      case 3:  return UNK3;
      case 4:  return CUBE;
      case 5:  return STASH;
      default:
        Gdx.app.error("BodyLoc", "Unknown store location: " + i);
        return null;
    }
  }
}
