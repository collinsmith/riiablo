package com.riiablo.io;

public class BitUtils {
  private BitUtils() {}

  public static boolean isUnsigned(long value, int bits) {
    assert 0 < bits : "bits(" + bits + ") < " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    return (value & (1L << (bits - 1))) == 0;
  }

  public static boolean isUnsigned(byte value) {
    return isUnsigned(value, Byte.SIZE);
  }

  public static boolean isUnsigned(short value) {
    return isUnsigned(value, Short.SIZE);
  }

  public static boolean isUnsigned(int value) {
    return isUnsigned(value, Integer.SIZE);
  }

  public static boolean isUnsigned(long value) {
    return isUnsigned(value, Long.SIZE);
  }
}
