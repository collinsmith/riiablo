package com.riiablo.io;

import io.netty.buffer.ByteBuf;

public class ByteOutput {
  private final ByteBuf buffer;
  private BitOutput bitOutput;

  ByteOutput(ByteBuf buffer) {
    this.buffer = buffer;
  }

  BitOutput bitOutput() {
    return bitOutput != null ? bitOutput : (bitOutput = new BitOutput(this));
  }

  public ByteBuf buffer() {
    return buffer;
  }

  public int bytesRemaining() {
    return buffer.writableBytes();
  }

  public boolean aligned() {
    throw null;
  }

  public BitOutput unalign() {
    return bitOutput();
  }

  long incrementBitsWritten(long bits) {
    assert (bits & (Byte.SIZE - 1)) == 0;
    if (bitOutput == null) return 0;
    return bitOutput.incrementBitsWritten(bits);
  }

  void _write8(long octet) {
    assert 0 <= octet && octet < (1 << Byte.SIZE);
    buffer.writeByte((int) octet);
  }

  public void write8(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Byte.SIZE);
    buffer.writeByte(value);
  }

  public void write16(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeShortLE(value);
  }

  public void write32(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeIntLE(value);
  }

  public void write64(long value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeLongLE(value);
  }
}
