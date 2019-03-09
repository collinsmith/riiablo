package com.riiablo.util;

public class ArrayUtils {
  private ArrayUtils() {}

  public static int firstPositive(int[] array) {
    for (int i : array) if (i > 0) return i;
    return 0;
  }

  public static short firstPositive(short[] array) {
    for (short i : array) if (i > 0) return i;
    return 0;
  }
}
