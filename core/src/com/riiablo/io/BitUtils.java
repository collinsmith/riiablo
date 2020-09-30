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

  public static int[] readSafe32u(ByteInput in, int len) {
    return readSafe32u(in, new int[len], 0, len);
  }

  public static int[] readSafe32u(final ByteInput in, final int[] dst, final int offset, final int len) {
    for (int i = offset; i < len; i++) dst[i] = in.readSafe32u();
    return dst;
  }
}
