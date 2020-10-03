package com.riiablo.io;

public class UnsafeNarrowing extends RuntimeException {
  public final ByteInput byteInput;
  public final long value;

  UnsafeNarrowing(ByteInput byteInput, long value) {
    super("value(" + value + ") cannot be safely narrowed!");
    this.byteInput = byteInput;
    this.value = value;
  }

  public ByteInput byteInput() {
    return byteInput;
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
