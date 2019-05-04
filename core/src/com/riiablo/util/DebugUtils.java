package com.riiablo.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DebugUtils {
  private DebugUtils() {}

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
}
