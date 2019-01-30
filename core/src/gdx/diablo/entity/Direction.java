package gdx.diablo.entity;

import com.badlogic.gdx.math.MathUtils;

public class Direction {
  public static final int SOUTH = 0;
  public static final int WEST  = 1;
  public static final int NORTH = 2;
  public static final int EAST  = 3;
  public static final int DOWN  = 4;
  public static final int LEFT  = 5;
  public static final int UP    = 6;
  public static final int RIGHT = 7;

  /*
  static private final int SIN_BITS[] = {3, 4, 5};
  static private final int SIN_MASK[] = {~(-1 << SIN_BITS[0]), ~(-1 << SIN_BITS[1]), ~(-1 << SIN_BITS[2])};
  static private final int SIN_COUNT[] = {SIN_MASK[0] + 1, SIN_MASK[1] + 1, SIN_MASK[2] + 1};

  static private final float radFull = MathUtils.PI2;
  static private final float radToIndex[] = {SIN_COUNT[0] / radFull, SIN_COUNT[1] / radFull, SIN_COUNT[2] / radFull};

  static final int DIRS[][] = {
      {7, 2, 6, 1, 5, 0, 4, 3},
      {7, 13, 2, 12, 6, 11, 1, 10, 5, 9, 0, 8, 4, 15, 3, 14},
      {7, 27, 13, 26, 2, 25, 12, 24, 6, 23, 11, 22, 1, 21, 10, 20, 5, 19, 9, 18, 0, 17, 8, 16, 4, 31, 15, 30, 3, 29, 14, 28}
  };
  */

  static final byte[][] OFFSETS = new byte[][] {
      { 0, 2},
      {-2, 0},
      { 0,-2},
      { 2, 0},

      { 2, 2},
      {-2, 2},
      {-2,-2},
      { 2,-2},

      { 1, 2},
      {-1, 2},
      {-2, 1},
      {-2,-1},
      {-1,-2},
      { 1,-2},
      { 2,-1},
      { 2, 1},
  };

  static final float[] RADIANS = new float[] {
      MathUtils.PI * 5 / 4,
      MathUtils.PI * 3 / 4,
      MathUtils.PI * 1 / 4,
      MathUtils.PI * 7 / 4,

      MathUtils.PI * 3 / 2,
      MathUtils.PI * 2 / 2,
      MathUtils.PI * 1 / 2,
      MathUtils.PI * 0 / 2,

      MathUtils.PI * 11 / 8,
      MathUtils.PI *  9 / 8,
      MathUtils.PI *  7 / 8,
      MathUtils.PI *  5 / 8,
      MathUtils.PI *  3 / 8,
      MathUtils.PI *  1 / 8,
      MathUtils.PI * 15 / 8,
      MathUtils.PI * 13 / 8,
  };

  private Direction() {}

  public static int radiansToDirection(float radians, int directions) {
    switch (directions) {
      case 1:  return 0;
      case 8:  return radiansToDirection8(radians);
      case 16: return radiansToDirection16(radians);
      default: return 0;
    }
  }

  static final int   DIRS_16[] = {7, 13, 2, 12, 6, 11, 1, 10, 5, 9, 0, 8, 4, 15, 3, 14};
  static final float RADIANS_16[];
  static {
    float r = MathUtils.PI2 / 32;
    float a = r * 2;
    RADIANS_16 = new float[16];
    for (int i = 0; i < 16; i++) {
      RADIANS_16[i] = r;
      r += a;
    }

    //System.out.println(Arrays.toString(RADIANS_16));
  }

  static final int   DIRS_8[] = {7, 2, 6, 1, 5, 0, 4, 3};
  static final float RADIANS_8[];
  static {
    float r = MathUtils.PI2 / 16;
    float a = r * 2;
    RADIANS_8 = new float[8];
    for (int i = 0; i < 8; i++) {
      RADIANS_8[i] = r;
      r += a;
    }

    //System.out.println(Arrays.toString(RADIANS_8));
  }

  public static int radiansToDirection16(float radians) {
    if (radians < 0) radians += MathUtils.PI2;
    for (int i = 0; i < 16; i++) {
      if (radians < RADIANS_16[i]) return DIRS_16[i];
    }

    return DIRS_16[0];
  }

  public static int radiansToDirection8(float radians) {
    if (radians < 0) radians += MathUtils.PI2;
    for (int i = 0; i < 8; i++) {
      if (radians < RADIANS_8[i]) return DIRS_8[i];
    }

    return DIRS_8[0];
  }

  public static int getOffX(float radians) {
    int id = radiansToDirection16(radians);
    return OFFSETS[id][0];
  }
  public static int getOffY(float radians) {
    int id = radiansToDirection16(radians);
    return OFFSETS[id][1];
  }

  public static float radiansToDirection16Radians(float radians) {
    if (radians < 0) radians += MathUtils.PI2;
    for (int i = 0; i < 16; i++) {
      if (radians < RADIANS_16[i]) return RADIANS[DIRS_16[i]];
    }

    return RADIANS[DIRS_16[0]];
  }

  /*
  public static int radiansToDirection(float radians, int directions) {
    int d;
    switch (directions) {
      case  8: d = 0; break;
      case 16: d = 1; break;
      case 32: d = 2; break;
      default: throw new GdxRuntimeException("Invalid directions num: " + directions);
    }

    int id = (int) (radians * radToIndex[d]) & SIN_MASK[d];
    System.out.println(id);
    return DIRS[d][id];
  }

  public static int getOffX(float radians) {
    int id = DIRS[1][(int) (radians * radToIndex[1]) & SIN_MASK[1]];
    switch (id) {
      case  8: id = 0; break;
      case  9: id = 0; break;
      case 10: id = 0; break;
      case 11: id = 0; break;
      case 12: id = 0; break;
      case 13: id = 0; break;
      case 14: id = 0; break;
      case 15: id = 0; break;
    }
    //System.out.println(id);
    return OFFSETS[id][0];
  }

  public static int getOffY(float radians) {
    int id = DIRS[0][(int) (radians * radToIndex[0]) & SIN_MASK[0]];
    return OFFSETS[id][1];
  }
  */
}
