package com.riiablo.io;

public class UnsafeNarrowing extends RuntimeException {
  public final long offset;
  public final long value;

  UnsafeNarrowing(long value) {
    this(-1, value);
  }

  UnsafeNarrowing(long offset, long value) {
    super("value(" + value + ") cannot be safely narrowed!");
    this.offset = offset;
    this.value = value;
  }

  public <R> R wrapAndThrow() {
    if (offset < 0) {
      throw this;
    } else {
      throw new InvalidFormat(offset, "Unsafe narrowing", this);
    }
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
