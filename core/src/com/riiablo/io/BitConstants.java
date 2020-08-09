package com.riiablo.io;

public class BitConstants {
  private BitConstants() {}

  static final long[] UNSIGNED_MASKS = new long[Long.SIZE];
  static {
    for (int i = 1; i < Long.SIZE; i++) {
      UNSIGNED_MASKS[i] = (UNSIGNED_MASKS[i - 1] << 1) + 1;
    }
  }
}
