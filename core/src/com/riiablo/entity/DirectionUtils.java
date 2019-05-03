package com.riiablo.entity;

public class DirectionUtils {
  private DirectionUtils() {}

  public static final int SOUTH = 0;
  public static final int WEST  = 1;
  public static final int NORTH = 2;
  public static final int EAST  = 3;
  public static final int DOWN  = 4;
  public static final int LEFT  = 5;
  public static final int UP    = 6;
  public static final int RIGHT = 7;

  static final float RADIANS_4[] = {
      -1.5707964f, // MathUtils.atan2(-4,  0),
       0.0f,       // MathUtils.atan2( 0,  8),
       1.5707964f, // MathUtils.atan2( 4,  0),
       3.1415927f, // MathUtils.atan2( 0, -8),
  };
  static final int   DIRS_4M[] = {1, 2, 3, 0};
  static final float RADIANS_4M[] = {
      -2.3561945f, -0.7853982f, 0.7853982f, 2.3561945f,
  };
  /*static {
    RADIANS_4M = new float[4];
    float min = -RADIANS_4[3];
    for (int i = 0; i < 4; i++) {
      RADIANS_4M[i] = (min + RADIANS_4[i]) / 2;
      min = RADIANS_4[i];
    }
  }*/

  static final float RADIANS_8[] = {
      -2.6743030f, // MathUtils.atan2(-2, -4),
      -1.5707964f, // RADIANS_4[0],
      -0.4672897f, // MathUtils.atan2(-2,  4),
       0.0f,       // RADIANS_4[1],
       0.4672897f, // MathUtils.atan2( 2,  4),
       1.5707964f, // RADIANS_4[2],
       2.6743030f, // MathUtils.atan2( 2, -4),
       3.1415927f, // RADIANS_4[3],
  };
  static final int   DIRS_8M[] = {1, 6, 2, 7, 3, 4, 0, 5};
  static final float RADIANS_8M[] = {
      -2.907948f, -2.1225498f, -1.019043f, -0.23364484f, 0.23364484f, 1.019043f, 2.1225498f, 2.907948f,
  };
  /*static {
    RADIANS_8M = new float[8];
    float min = -RADIANS_8[7];
    for (int i = 0; i < 8; i++) {
      RADIANS_8M[i] = (min + RADIANS_8[i]) / 2;
      min = RADIANS_8[i];
    }
  }*/

  static final float RADIANS_16[] = {
      -2.97621230f, // MathUtils.atan2(-1, -6),
      -2.67430300f, // RADIANS_8[0],
      -2.16368170f, // MathUtils.atan2(-3, -2),
      -1.57079640f, // RADIANS_8[1],
      -0.97791100f, // MathUtils.atan2(-3,  2),
      -0.46728970f, // RADIANS_8[2],
      -0.16538037f, // MathUtils.atan2(-1,  6),
       0.0f,        // RADIANS_8[3],
       0.16538037f, // MathUtils.atan2( 1,  6),
       0.46728970f, // RADIANS_8[4],
       0.97791100f, // MathUtils.atan2( 3,  2),
       1.57079640f, // RADIANS_8[5],
       2.16368170f, // MathUtils.atan2( 3, -2),
       2.67430300f, // RADIANS_8[6],
       2.97621230f, // MathUtils.atan2( 1, -6),
       3.14159270f, // RADIANS_8[7],
  };
  static final int   DIRS_16M[] = {1, 11, 6, 12, 2, 13, 7, 14, 3, 15, 4, 8, 0, 9, 5, 10};
  static final float RADIANS_16M[] = {
      -3.0589025f, -2.8252578f, -2.4189925f, -1.867239f, -1.2743537f, -0.72260034f, -0.31633502f,
      -0.08269019f, 0.08269019f, 0.31633502f, 0.72260034f, 1.2743537f, 1.867239f, 2.4189925f,
      2.8252578f, 3.0589025f
  };
  /*static {
    RADIANS_16M = new float[16];
    float min = -RADIANS_16[15];
    for (int i = 0; i < 16; i++) {
      RADIANS_16M[i] = (min + RADIANS_16[i]) / 2;
      min = RADIANS_16[i];
    }
  }*/

  static final float RADIANS_32[] = {
      -3.07026600f, // MathUtils.atan2(-0.5f, -7),
      -2.97621230f, // RADIANS_16[0],
      -2.84896680f, // MathUtils.atan2(-1.5f, -5),
      -2.67430300f, // RADIANS_16[1],
      -2.44391820f, // MathUtils.atan2(-2.5f, -3),
      -2.16368170f, // RADIANS_16[2],
      -1.85012600f, // MathUtils.atan2(-3.5f, -1),
      -1.57079640f, // RADIANS_16[3],
      -1.29146670f, // MathUtils.atan2(-3.5f, 1),
      -0.97791100f, // RADIANS_16[4],
      -0.69767440f, // MathUtils.atan2(-2.5f, 3),
      -0.46728970f, // RADIANS_16[5],
      -0.29262584f, // MathUtils.atan2(-1.5f, 5),
      -0.16538037f, // RADIANS_16[6],
      -0.07132668f, // MathUtils.atan2(-0.5f, 7),
       0.0f,        // RADIANS_16[7],
       0.07132668f, // MathUtils.atan2(0.5f, 7),
       0.16538037f, // RADIANS_16[8],
       0.29262584f, // MathUtils.atan2(1.5f, 5),
       0.46728970f, // RADIANS_16[9],
       0.69767440f, // MathUtils.atan2(2.5f, 3),
       0.97791100f, // RADIANS_16[10],
       1.29146670f, // MathUtils.atan2(3.5f, 1),
       1.57079640f, // RADIANS_16[11],
       1.85012600f, // MathUtils.atan2(3.5f, -1),
       2.16368170f, // RADIANS_16[12],
       2.44391820f, // MathUtils.atan2(2.5f, -3),
       2.67430300f, // RADIANS_16[13],
       2.84896680f, // MathUtils.atan2(1.5f, -5),
       2.97621230f, // RADIANS_16[14],
       3.07026600f, // MathUtils.atan2(0.5f, -7),
       3.14159270f, // RADIANS_16[15],
  };

  static final int   DIRS_32M[] = {1, 22, 11, 23, 6, 24, 12, 25, 2, 26, 13, 27, 7, 28, 14, 29, 3, 30, 15, 31, 4, 16, 8, 17, 0, 18, 9, 19, 5, 20, 10, 21};
  static final float RADIANS_32M[] = {
      -3.1059294f, -3.0232391f, -2.9125896f, -2.7616348f, -2.5591106f, -2.3038f, -2.006904f,
      -1.7104611f, -1.4311316f, -1.1346889f, -0.8377927f, -0.58248204f, -0.37995777f, -0.2290031f,
      -0.11835353f, -0.03566334f, 0.03566334f, 0.11835353f, 0.2290031f, 0.37995777f, 0.58248204f,
      0.8377927f, 1.1346889f, 1.4311316f, 1.7104611f, 2.006904f, 2.3038f, 2.5591106f, 2.7616348f,
      2.9125896f, 3.0232391f, 3.1059294f
  };
  /*static {
    RADIANS_32M = new float[32];
    float min = -RADIANS_32[31];
    for (int i = 0; i < 32; i++) {
      RADIANS_32M[i] = (min + RADIANS_32[i]) / 2;
      min = RADIANS_32[i];
    }
  }*/

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
    int index = (radians < RADIANS_4M[1])   ? 0 : 2;
    index |= (radians <  RADIANS_4M[index]) ? 0 : 1;
    index &= (radians >= RADIANS_4M[3    ]) ? 0 : index;
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
    int index = (radians < RADIANS_8M[3])     ? 0 : 4;
    index |= (radians <  RADIANS_8M[index|1]) ? 0 : 2;
    index |= (radians <  RADIANS_8M[index  ]) ? 0 : 1;
    index &= (radians >= RADIANS_8M[7      ]) ? 0 : index;
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
    int index = (radians < RADIANS_16M[7])     ? 0 : 8;
    index |= (radians <  RADIANS_16M[index|3]) ? 0 : 4;
    index |= (radians <  RADIANS_16M[index|1]) ? 0 : 2;
    index |= (radians <  RADIANS_16M[index  ]) ? 0 : 1;
    index &= (radians >= RADIANS_16M[15     ]) ? 0 : index;
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
    int index = (radians < RADIANS_32M[15])    ? 0 : 16;
    index |= (radians <  RADIANS_32M[index|7]) ? 0 : 8;
    index |= (radians <  RADIANS_32M[index|3]) ? 0 : 4;
    index |= (radians <  RADIANS_32M[index|1]) ? 0 : 2;
    index |= (radians <  RADIANS_32M[index  ]) ? 0 : 1;
    index &= (radians >= RADIANS_32M[31     ]) ? 0 : index;
    // above line should remove need for initial conditional statement
    return DIRS_32M[index];
  }
}
