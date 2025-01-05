package com.riiablo.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

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

  private static final float FONT_HEIGHT = 9f; // Height of the font in points
  private static final float FONT_WIDTH = 6f; // Approximate width of the font
  private static final float LETTER_SPACING = 2f;

  private static final float[][] SEGMENT_COORDS = {
      {0, FONT_HEIGHT, FONT_WIDTH, FONT_HEIGHT},
      {FONT_WIDTH, FONT_HEIGHT, FONT_WIDTH, FONT_HEIGHT / 2},
      {FONT_WIDTH, FONT_HEIGHT / 2, FONT_WIDTH, 0},
      {0, 0, FONT_WIDTH, 0},
      {0, FONT_HEIGHT / 2, 0, 0},
      {0, FONT_HEIGHT, 0, FONT_HEIGHT / 2},
      {0, FONT_HEIGHT / 2, FONT_WIDTH / 2, FONT_HEIGHT / 2},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, FONT_WIDTH, FONT_HEIGHT / 2},
      {0, FONT_HEIGHT, FONT_WIDTH / 2, FONT_HEIGHT / 2},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, FONT_WIDTH / 2, FONT_HEIGHT},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, FONT_WIDTH, FONT_HEIGHT},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, 0, 0},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, FONT_WIDTH / 2, 0},
      {FONT_WIDTH / 2, FONT_HEIGHT / 2, FONT_WIDTH, 0},
  };

  public static final int[] CHARACTER_MAP = new int[128];
  static {
    // Default: All unsupported characters have all segments on
    for (int i = 0; i < 128; i++) {
      CHARACTER_MAP[i] = 0x3FFF;
    }

    // Space (' ')
    CHARACTER_MAP[' '] = 0x0000; // No segments on

    // Digits (0-9)
    CHARACTER_MAP['0'] = 0x0C3F;
    CHARACTER_MAP['1'] = 0x1200;
    CHARACTER_MAP['2'] = 0x00DB;
    CHARACTER_MAP['3'] = 0x008F;
    CHARACTER_MAP['4'] = 0x00E6;
    CHARACTER_MAP['5'] = 0x00ED;
    CHARACTER_MAP['6'] = 0x00FD;
    CHARACTER_MAP['7'] = 0x1401;
    CHARACTER_MAP['8'] = 0x00FF;
    CHARACTER_MAP['9'] = 0x00E7;

    // Uppercase Letters (A-Z)
    CHARACTER_MAP['A'] = 0x00F7;
    CHARACTER_MAP['B'] = 0x128F;
    CHARACTER_MAP['C'] = 0x0039;
    CHARACTER_MAP['D'] = 0x120F;
    CHARACTER_MAP['E'] = 0x00F9;
    CHARACTER_MAP['F'] = 0x00F1;
    CHARACTER_MAP['G'] = 0x00BD;
    CHARACTER_MAP['H'] = 0x00F6;
    CHARACTER_MAP['I'] = 0x1209;
    CHARACTER_MAP['J'] = 0x001E;
    CHARACTER_MAP['K'] = 0x2470;
    CHARACTER_MAP['L'] = 0x0038;
    CHARACTER_MAP['M'] = 0x0536;
    CHARACTER_MAP['N'] = 0x2136;
    CHARACTER_MAP['O'] = 0x003F;
    CHARACTER_MAP['P'] = 0x00F3;
    CHARACTER_MAP['Q'] = 0x203F;
    CHARACTER_MAP['R'] = 0x20F3;
    CHARACTER_MAP['S'] = 0x018D;
    CHARACTER_MAP['T'] = 0x1201;
    CHARACTER_MAP['U'] = 0x003E;
    CHARACTER_MAP['V'] = 0x0C30;
    CHARACTER_MAP['W'] = 0x2836;
    CHARACTER_MAP['X'] = 0x2D00;
    CHARACTER_MAP['Y'] = 0x1500;
    CHARACTER_MAP['Z'] = 0x0C09;

    // Lowercase Letters ('a'-'z')
    for (char c = 'a'; c <= 'z'; c++) {
      CHARACTER_MAP[c] = CHARACTER_MAP[c - 32]; // Map to uppercase
    }
  }

  public static void drawAscii(ShapeRenderer shapes, String text, float x, float y, int align) {
    if (Align.isTop(align)) y -= FONT_HEIGHT;
    // TODO: support additional alignment options
    for (char c : text.toCharArray()) {
      drawAscii(shapes, c, x, y);
      x += FONT_WIDTH + LETTER_SPACING;
    }
  }

  public static void drawAscii(ShapeRenderer shapeRenderer, char c, float x, float y) {
    int segments = c < CHARACTER_MAP.length ? CHARACTER_MAP[c] : 0x3FFF;
    for (int i = 0; i < SEGMENT_COORDS.length; i++) {
      if ((segments & (1 << i)) != 0) {
        float[] coord = SEGMENT_COORDS[i];
        float x1 = x + coord[0];
        float y1 = y + coord[1];
        float x2 = x + coord[2];
        float y2 = y + coord[3];
        shapeRenderer.line(x1, y1, x2, y2);
      }
    }
  }
}
