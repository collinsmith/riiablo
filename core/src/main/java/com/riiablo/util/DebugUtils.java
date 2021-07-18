package com.riiablo.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.riiablo.Riiablo;

public class DebugUtils {
  private DebugUtils() {}

  public static String getDifficultyString(int diff) {
    switch (diff) {
      case Riiablo.NORMAL:    return "NORM";
      case Riiablo.NIGHTMARE: return "NIGHTMARE";
      case Riiablo.HELL:      return "HELL";
      default:
        throw new IllegalArgumentException(
            "diff(" + diff + ") is not a valid difficulty");
    }
  }

  public static String getClassString(int classId) {
    switch (classId) {
      case Riiablo.AMAZON:      return "AMAZON";
      case Riiablo.SORCERESS:   return "SORCERESS";
      case Riiablo.NECROMANCER: return "NECROMANCER";
      case Riiablo.PALADIN:     return "PALADIN";
      case Riiablo.BARBARIAN:   return "BARBARIAN";
      case Riiablo.DRUID:       return "DRUID";
      case Riiablo.ASSASSIN:    return "ASSASSIN";
      default:
        throw new IllegalArgumentException(
            "classId(" + classId + ") is not a valid class id");
    }
  }

  public static String toByteArray(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (byte b : bytes) sb.append(String.format("%02X", b & 0xFF)).append(", ");
    if (sb.length() > 1) sb.setLength(sb.length() - 2);
    sb.append("]");
    return sb.toString();
  }

  public static void drawDiamond2(ShapeRenderer shapes, float x, float y, int width, int height) {
    int hw = width  >>> 1;
    int hh = height >>> 1;
    if (shapes.getCurrentType() == ShapeRenderer.ShapeType.Filled) {
      shapes.triangle(x, y + hh, x + hw, y + height, x + width, y + hh);
      shapes.triangle(x, y + hh, x + hw, y         , x + width, y + hh);
    } else {
      shapes.line(x        , y + hh    , x + hw   , y + height);
      shapes.line(x + hw   , y + height, x + width, y + hh    );
      shapes.line(x + width, y + hh    , x + hw   , y         );
      shapes.line(x + hw   , y         , x        , y + hh    );
    }
  }

  public static void drawDiamond(ShapeRenderer shapes, float x, float y, int width, int height) {
    drawDiamond2(shapes, x - width / 2, y - height / 2, width, height);
  }

  public static void drawDiamondTL(ShapeRenderer shapes, float x, float y, int width, int height) {
    height = Math.abs(height);
    drawDiamond2(shapes, x, y - height, width, height);
  }

  public static void drawEllipse2(ShapeRenderer shapes, float x, float y, int width, int height) {
    shapes.ellipse(x - width / 2, y - height / 2, width, height);
  }

  public static void drawEllipse(ShapeRenderer shapes, float x, float y, int width, int height) {
    drawEllipse2(shapes, x - width / 2, y - height / 2, width, height);
  }
}
