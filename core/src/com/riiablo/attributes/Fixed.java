package com.riiablo.attributes;

public class Fixed {
  public static int floatToIntBits(float value, int precision) {
    return (int) (value * (1 << precision));
  }

  public static float intBitsToFloat(int value, int precision) {
    final int pow2 = (1 << precision);
    final int mask = pow2 - 1;
    return ((value >>> precision) + ((value & mask) / (float) pow2));
  }

  public static int intBitsToFloatFloor(int value, int precision) {
    return value >>> precision;
  }

  private Fixed() {}
}
