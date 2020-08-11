package com.riiablo.io;

public class SafeUnsigned extends RuntimeException {
  public final long value;

  SafeUnsigned(long value) {
    super("value(" + value + ") is not unsigned!");
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
