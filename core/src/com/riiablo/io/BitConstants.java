package com.riiablo.io;

public class BitConstants {
  private BitConstants() {}

  static final int MAX_ULONG_BITS = Long.SIZE - 1;
  static final int MAX_UINT_BITS = Integer.SIZE - 1;
  static final int MAX_USHORT_BITS = Short.SIZE - 1;
  static final int MAX_UBYTE_BITS = Byte.SIZE - 1;
  static final int MAX_UNSIGNED_BITS = MAX_ULONG_BITS;

  static final int MAX_SAFE_CACHED_BITS = Long.SIZE - Byte.SIZE;

  static final int BYTE_SHIFT = Integer.bitCount(Byte.SIZE - 1);
  static final int BYTE_MASK = (1 << Byte.SIZE) - 1;

  static final long[] UNSIGNED_MASKS = new long[Long.SIZE + 1];
  static {
    for (int i = 1; i < Long.SIZE; i++) {
      UNSIGNED_MASKS[i] = (UNSIGNED_MASKS[i - 1] << 1) + 1;
    }
  }
}
