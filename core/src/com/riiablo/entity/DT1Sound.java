package com.riiablo.entity;

import com.riiablo.codec.excel.Levels;
import com.riiablo.map.DT1;

public class DT1Sound {

  private DT1Sound() {}

  public static String getType(Levels.Entry levels, DT1.Tile tile) {
    if (tile == null) return "dirt";
    int soundIndex = tile.soundIndex & 0xFF;
    switch (levels.LevelType) {
      case 1:  return getType1(soundIndex);
      case 2:  return getType2(soundIndex);
      case 3:  return getType3(soundIndex);
      default: return "dirt";
    }

    /**
     * best guess:
     *
     * level type determines table
     * soundIndex determines which sound to pull from table
     * NOT ALWAYS POWER OF 2 -- some tiles in act 2 town are 1,17,65,129
     * it's possible they are flags to represent random, i.e., 17 = random 1 or 16
     */
  }

  private static String getType1(int soundIndex) {
    switch (soundIndex) {
      case 0:  return "dirt";
      default: return "dirt";
    }
  }

  private static String getType2(int soundIndex) {
    switch (soundIndex) {
      case 0:   return "dirt";
      case 128: return "wood";
      default:  return "dirt";
    }
  }

  private static String getType3(int soundIndex) {
    switch (soundIndex) {
      case 0:   return "dirt";
      default:  return "dirt";
    }
  }
}
