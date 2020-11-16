package com.riiablo.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import com.riiablo.codec.excel.Levels;

public enum Material {
  DIRT(new Color(0x4F7942FF)),
  WOOD(new Color(0x654321FF)),
  ISTONE(new Color(0x708090FF)),
  OSTONE(new Color(0xA9A9A9FF)),
  SAND(new Color(0xedc9afFF)),
  SNOW(new Color(0xfffafaFF)),
  ;

  final String name;
  final Color color;

  Material() {
    this(new Color(MathUtils.random.nextInt() | 0xFF));
  }

  Material(Color color) {
    this.name = name().toLowerCase();
    this.color = color;
  }

  Material(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * PORTED FROM OLD ENGINE
   *
   * best guess:
   *
   * level type determines table
   * soundIndex determines which sound to pull from table
   * NOT ALWAYS POWER OF 2 -- some tiles in act 2 town are 1,17,65,129
   * it's possible they are flags to represent random, i.e., 17 = random 1 or 16
   */
  public static Material getMaterial(Levels.Entry level, DT1.Tile tile) {
    if (tile == null) return Material.DIRT;
    int soundIndex = tile.soundIndex & 0xFF;
    switch (level.LevelType) {
      case 1:  return getType1(soundIndex);
      case 2:  return getType2(soundIndex);
      case 3:  return getType3(soundIndex);
      default: return Material.DIRT;
    }
  }

  private static Material getType1(int soundIndex) {
    switch (soundIndex) {
      case 0:  return Material.DIRT;
      default: return Material.DIRT;
    }
  }

  private static Material getType2(int soundIndex) {
    switch (soundIndex) {
      case 0:   return Material.DIRT;
      case 128: return Material.WOOD;
      default:  return Material.DIRT;
    }
  }

  private static Material getType3(int soundIndex) {
    switch (soundIndex) {
      case 0:   return Material.DIRT;
      default:  return Material.DIRT;
    }
  }
}
