package com.riiablo.math;

public final class Fixed {
  private static final int[] DIVISOR = new int[Integer.SIZE];
  static {
    for (int i = 0; i < Integer.SIZE; i++) {
      DIVISOR[i] = 1 << i;
    }
  }

  public static int floatToIntBits(final float value, final int precision) {
    return (int) (value * DIVISOR[precision]);
  }

  public static float intBitsToFloat(final int value, final int precision) {
    final int pow2 = DIVISOR[precision];
    final int mask = pow2 - 1;
    return ((value >>> precision) + ((value & mask) / (float) pow2));
  }

  public static int intBitsToFloatFloor(final int value, final int precision) {
    return value >>> precision;
  }

  @Deprecated
  public static boolean isNegative(final int value) {
    return value >> 31 != 0; // TODO: add negative encoding support to above functions
  }

  private Fixed() {}
}
