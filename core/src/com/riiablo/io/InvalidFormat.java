package com.riiablo.io;

public class InvalidFormat extends RuntimeException {
  public final long offset;

  public InvalidFormat(ByteInput in, String message) {
    this(in, message, null);
  }

  public InvalidFormat(ByteInput in, Throwable cause) {
    this(in, "Invalid format", cause);
  }

  public InvalidFormat(ByteInput in, String message, Throwable cause) {
    this(in.mark(), message, cause);
  }

  InvalidFormat(long offset, String message, Throwable cause) {
    super(message + " +0x" + Long.toHexString(offset), cause);
    this.offset = offset;
  }

  @Deprecated
  public InvalidFormat(String message) {
    super(message, null);
    this.offset = 0L;
  }

  public long offset() {
    return offset;
  }
}
