package com.riiablo.io;

public class UnsafeNarrowing extends RuntimeException {
  public final long value;

  UnsafeNarrowing(long value) {
    super("value(" + value + ") cannot be safely narrowed!");
    this.value = value;
  }

  public short u8() {
    return (short) value;
  }

  public int u16() {
    return (int) value;
  }

  public long u32() {
    return (long) value;
  }
}
