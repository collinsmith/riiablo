package com.riiablo.util;

public class ArrayUtils {
  private ArrayUtils() {}

  public static boolean allZeroes(byte[] array) {
    for (byte b : array) if (b != 0) return false;
    return true;
  }

  public static int firstPositive(int[] array) {
    for (int i : array) if (i > 0) return i;
    return 0;
  }

  public static short firstPositive(short[] array) {
    for (short i : array) if (i > 0) return i;
    return 0;
  }

  public static byte[] toByteArray(int[] array) {
    byte[] b = new byte[array.length];
    for (int i = 0; i < array.length; i++) b[i] = (byte) array[i];
    return b;
  }

  public static byte[] toFixedPoint(float[] array) {
    byte[] b = new byte[array.length];
    for (int i = 0; i < array.length; i++) b[i] = (byte) (array[i] * 255f);
    return b;
  }

  public static float[] toFloatingPoint(byte[] array) {
    float[] f = new float[array.length];
    for (int i = 0; i < array.length; i++) f[i] = (array[i] & 0xFF) / 255f;
    return f;
  }
}
