package com.riiablo.io;

import io.netty.buffer.ByteBufUtil;

public class SignatureMismatch extends InvalidFormat {
  public final byte[] actual;
  public final byte[] expected;

  public SignatureMismatch(ByteInput in, byte[] actual, byte[] expected) {
    super(
        in,
        String.format("Signatures do not match: %s, expected %s",
            ByteBufUtil.hexDump(actual),
            ByteBufUtil.hexDump(expected)));
    this.actual = actual;
    this.expected = expected;
  }

  public byte[] actual() {
    return actual;
  }

  public byte[] expected() {
    return expected;
  }
}
