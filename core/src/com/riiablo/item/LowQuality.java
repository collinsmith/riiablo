package com.riiablo.item;

import com.badlogic.gdx.Gdx;

public enum LowQuality {
  CRUDE      (1726),
  CRACKED    (1725),
  DAMAGED    (1724),
  LOW_QUALITY(1723);

  public int stringId;

  LowQuality(int stringId) {
    this.stringId = stringId;
  }

  public static LowQuality valueOf(int i) {
    switch (i) {
      case 0: return CRUDE;
      case 1: return CRACKED;
      case 2: return DAMAGED;
      case 3: return LOW_QUALITY;
      default:
        Gdx.app.error("LowQuality", "Unknown low quality: " + i);
        return null;
    }
  }
}
