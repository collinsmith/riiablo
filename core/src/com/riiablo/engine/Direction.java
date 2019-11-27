package com.riiablo.engine;

import com.badlogic.gdx.math.MathUtils;

import org.apache.commons.lang3.ArrayUtils;

public class Direction {
  private Direction() {}

  public static final int SOUTH = 0;
  public static final int WEST  = 1;
  public static final int NORTH = 2;
  public static final int EAST  = 3;
  public static final int DOWN  = 4;
  public static final int LEFT  = 5;
  public static final int UP    = 6;
  public static final int RIGHT = 7;

  static final float RADIANS_4[] = {
      -MathUtils.PI,
      -MathUtils.PI / 2,
       0,
       MathUtils.PI / 2,
  };
  static final int   DIRS_4M[] = {1, 2, 3, 0};
  static final float RADIANS_4M[] = {
      -MathUtils.PI * 3 / 4,
      -MathUtils.PI * 1 / 4,
       MathUtils.PI * 1 / 4,
       MathUtils.PI * 3 / 4,
  };

  static final float RADIANS_8[] = {
      RADIANS_4[0],
      RADIANS_4M[0],
      RADIANS_4[1],
      RADIANS_4M[1],
      RADIANS_4[2],
      RADIANS_4M[2],
      RADIANS_4[3],
      RADIANS_4M[3],
  };
  static final int   DIRS_8M[] = {1, 6, 2, 7, 3, 4, 0, 5};
  static final float RADIANS_8M[];
  static {
    RADIANS_8M = new float[8];
    float max = -RADIANS_8[0];
    for (int i = 7; i >= 0; i--) {
      RADIANS_8M[i] = (max + RADIANS_8[i]) / 2;
      max = RADIANS_8[i];
    }
  }

  static final float RADIANS_16[] = {
      RADIANS_8[0],
      RADIANS_8M[0],
      RADIANS_8[1],
      RADIANS_8M[1],
      RADIANS_8[2],
      RADIANS_8M[2],
      RADIANS_8[3],
      RADIANS_8M[3],
      RADIANS_8[4],
      RADIANS_8M[4],
      RADIANS_8[5],
      RADIANS_8M[5],
      RADIANS_8[6],
      RADIANS_8M[6],
      RADIANS_8[7],
      RADIANS_8M[7],
  };
  static final int   DIRS_16M[] = {1, 11, 6, 12, 2, 13, 7, 14, 3, 15, 4, 8, 0, 9, 5, 10};
  static final float RADIANS_16M[];
  static {
    RADIANS_16M = new float[16];
    float max = -RADIANS_16[0];
    for (int i = 15; i >= 0; i--) {
      RADIANS_16M[i] = (max + RADIANS_16[i]) / 2;
      max = RADIANS_16[i];
    }
  }

  static final float RADIANS_32[] = {
      RADIANS_16[0],
      RADIANS_16M[0],
      RADIANS_16[1],
      RADIANS_16M[1],
      RADIANS_16[2],
      RADIANS_16M[2],
      RADIANS_16[3],
      RADIANS_16M[3],
      RADIANS_16[4],
      RADIANS_16M[4],
      RADIANS_16[5],
      RADIANS_16M[5],
      RADIANS_16[6],
      RADIANS_16M[6],
      RADIANS_16[7],
      RADIANS_16M[7],
      RADIANS_16[8],
      RADIANS_16M[8],
      RADIANS_16[9],
      RADIANS_16M[9],
      RADIANS_16[10],
      RADIANS_16M[10],
      RADIANS_16[11],
      RADIANS_16M[11],
      RADIANS_16[12],
      RADIANS_16M[12],
      RADIANS_16[13],
      RADIANS_16M[13],
      RADIANS_16[14],
      RADIANS_16M[14],
      RADIANS_16[15],
      RADIANS_16M[15],
  };
  static final int   DIRS_32M[] = {1, 22, 11, 23, 6, 24, 12, 25, 2, 26, 13, 27, 7, 28, 14, 29, 3, 30, 15, 31, 4, 16, 8, 17, 0, 18, 9, 19, 5, 20, 10, 21};
  static final float RADIANS_32M[];
  static {
    RADIANS_32M = new float[32];
    float max = -RADIANS_32[0];
    for (int i = 31; i >= 0; i--) {
      RADIANS_32M[i] = (max + RADIANS_32[i]) / 2;
      max = RADIANS_32[i];
    }
  }

  public static int radiansToDirection(float radians, int directions) {
    switch (directions) {
      case 1:  return 0;
      case 4:  return radiansToDirection4(radians);
      case 8:  return radiansToDirection8(radians);
      case 16: return radiansToDirection16(radians);
      case 32: return radiansToDirection32(radians);
      default: return 0;
    }
  }

  @Deprecated
  static int _radiansToDirection4(float radians) {
    for (int i = 0; i < 4; i++) {
      if (radians < RADIANS_4M[i]) {
        return DIRS_4M[i];
      }
    }

    return DIRS_4M[0];
  }

  static int radiansToDirection4(float radians) {
    if (radians >= RADIANS_4M[3]) return DIRS_4M[0];
    int index = (radians < RADIANS_4M[1])  ? 0 : 2;
    index |= (radians < RADIANS_4M[index]) ? 0 : 1;
    return DIRS_4M[index];
  }

  @Deprecated
  static int _radiansToDirection8(float radians) {
    for (int i = 0; i < 8; i++) {
      if (radians < RADIANS_8M[i]) {
        return DIRS_8M[i];
      }
    }

    return DIRS_8M[0];
  }

  static int radiansToDirection8(float radians) {
    if (radians >= RADIANS_8M[7]) return DIRS_8M[0];
    int index = (radians < RADIANS_8M[3])    ? 0 : 4;
    index |= (radians < RADIANS_8M[index|1]) ? 0 : 2;
    index |= (radians < RADIANS_8M[index  ]) ? 0 : 1;
    return DIRS_8M[index];
  }

  @Deprecated
  static int _radiansToDirection16(float radians) {
    for (int i = 0; i < 16; i++) {
      if (radians < RADIANS_16M[i]) {
        return DIRS_16M[i];
      }
    }

    return DIRS_16M[0];
  }

  static int radiansToDirection16(float radians) {
    if (radians >= RADIANS_16M[15]) return DIRS_16M[0];
    int index = (radians < RADIANS_16M[7])    ? 0 : 8;
    index |= (radians < RADIANS_16M[index|3]) ? 0 : 4;
    index |= (radians < RADIANS_16M[index|1]) ? 0 : 2;
    index |= (radians < RADIANS_16M[index  ]) ? 0 : 1;
    return DIRS_16M[index];
  }

  @Deprecated
  static int _radiansToDirection32(float radians) {
    for (int i = 0; i < 32; i++) {
      if (radians < RADIANS_32M[i]) {
        return DIRS_32M[i];
      }
    }

    return DIRS_32M[0];
  }

  static int radiansToDirection32(float radians) {
    if (radians >= RADIANS_32M[31]) return DIRS_32M[0];
    int index = (radians < RADIANS_32M[15])   ? 0 : 16;
    index |= (radians < RADIANS_32M[index|7]) ? 0 : 8;
    index |= (radians < RADIANS_32M[index|3]) ? 0 : 4;
    index |= (radians < RADIANS_32M[index|1]) ? 0 : 2;
    index |= (radians < RADIANS_32M[index  ]) ? 0 : 1;
    return DIRS_32M[index];
  }

  public static float snapToDirection(float radians, int directions) {
    switch (directions) {
      case 1:  return RADIANS_4[0];
      case 4:  return _snapToDirection(radians, directions, DIRS_4M, RADIANS_4);
      case 8:  return _snapToDirection(radians, directions, DIRS_8M, RADIANS_8);
      case 16: return _snapToDirection(radians, directions, DIRS_16M, RADIANS_16);
      case 32: return _snapToDirection(radians, directions, DIRS_32M, RADIANS_32);
      default: return RADIANS_4[0];
    }
  }

  private static float _snapToDirection(float radians, int dirs, int[] dirsm, float[] rads) {
    int dir = radiansToDirection(radians, dirs);
    int index = ArrayUtils.indexOf(dirsm, dir);
    if (--index < 0) index = dirs - 1;
    return rads[index];
  }

  public static float direction8ToRadians(int direction) {
    int i = ArrayUtils.indexOf(DIRS_8M, direction);
    if (i == ArrayUtils.INDEX_NOT_FOUND) return 0;
    return RADIANS_8[i];
  }
}
