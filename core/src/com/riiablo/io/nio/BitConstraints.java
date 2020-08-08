package com.riiablo.io.nio;

class BitConstraints {
  private BitConstraints() {}

  private static int _validateSize(int min, int max, int bits) {
    assert min >= 0 : "min(" + min + ") < " + 0 + "!";
    assert max > 0 : "max(" + max + ") <= " + 0;
    assert min <= max : "min(" + min + ") > max(" + max + ")";
    if (bits < min) {
      throw new IllegalArgumentException("bits(" + bits + ") < " + min);
    }
    if (bits > max) {
      throw new IllegalArgumentException("bits(" + bits + ") > " + max);
    }
    return bits;
  }

  private static int validateSizeU(int max, int bits) {
    return _validateSize(0, max - 1, bits);
  }

  public static int validate7u(int bits) {
    return validateSizeU(Byte.SIZE, bits);
  }

  public static int validate15u(int bits) {
    return validateSizeU(Short.SIZE, bits);
  }

  public static int validate31u(int bits) {
    return validateSizeU(Integer.SIZE, bits);
  }

  public static int validate63u(int bits) {
    return validateSizeU(Long.SIZE, bits);
  }

  private static int validateSize(int max, int bits) {
    return _validateSize(0, max, bits);
  }

  public static int validate8(int bits) {
    return validateSize(Byte.SIZE, bits);
  }

  public static int validate16(int bits) {
    return validateSize(Short.SIZE, bits);
  }

  public static int validate32(int bits) {
    return validateSize(Integer.SIZE, bits);
  }

  public static int validate64(int bits) {
    return validateSize(Long.SIZE, bits);
  }

  public static boolean isUnsigned(long value, int size) {
    assert 0 < size && size <= Long.SIZE;
    return (value & (1 << (size - 1))) == 0;
  }

  public static int validateAscii(int bits) {
    return _validateSize(Byte.SIZE - 1, Byte.SIZE, bits);
  }
}
