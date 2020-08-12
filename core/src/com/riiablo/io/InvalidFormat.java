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
    super(message + " @0x" + Integer.toHexString(in.bytesRead()), cause);
    this.offset = in.bytesRead();
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
