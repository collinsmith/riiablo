package com.riiablo.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class ByteOutput {
  public static ByteOutput wrap(byte[] bytes) {
    return wrap(Unpooled.wrappedBuffer(bytes));
  }

  public static ByteOutput wrap(ByteBuf buffer) {
    return new ByteOutput(buffer);
  }

  private final ByteBuf buffer;
  private BitOutput bitOutput;

  ByteOutput(ByteBuf buffer) {
    assert !buffer.isReadOnly();
    this.buffer = buffer;
  }

  BitOutput bitOutput() {
    return bitOutput != null ? bitOutput : (bitOutput = new BitOutput(this));
  }

  /**
   * Returns a read-only view of the underlying byte buffer. This method is
   * provided for debugging and testing purposes. <b>Do not use this in
   * production code!</b>
   */
  public ByteBuf buffer() {
    return buffer.asReadOnly();
  }

  public int bytesWritten() {
    return buffer.writerIndex();
  }

  public int bytesRemaining() {
    return buffer.writableBytes();
  }

  public boolean aligned() {
    return bitOutput == null || bitOutput.aligned();
  }

  public BitOutput unalign() {
    return bitOutput();
  }

  public ByteOutput skipBytes(int bytes) {
    assert aligned() : "not aligned";
    incrementBitsWritten((long) bytes * Byte.SIZE);
    buffer.writeZero(bytes);
    return this;
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

  public ByteOutput writeBoolean(boolean b) {
    return write8(b ? 1 : 0);
  }

  public ByteOutput write8(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Byte.SIZE);
    buffer.writeByte(value);
    return this;
  }

  public ByteOutput write16(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeShortLE(value);
    return this;
  }

  public ByteOutput write32(int value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeIntLE(value);
    return this;
  }

  public ByteOutput write64(long value) {
    assert aligned() : "not aligned";
    incrementBitsWritten(Short.SIZE);
    buffer.writeLongLE(value);
    return this;
  }

  public ByteOutput writeBytes(byte[] src) {
    assert aligned() : "not aligned";
    return writeBytes(src, 0, src.length);
  }

  public ByteOutput writeBytes(byte[] src, int srcOffset, int len) {
    assert aligned() : "not aligned";
    incrementBitsWritten((long) len * Byte.SIZE);
    buffer.writeBytes(src, srcOffset, len);
    return this;
  }

  // TODO: create readChars int com.riiablo.io.BitInput
  //       Chars methods for explicit chars
  //       String methods for implicit null-termination
  public ByteOutput writeChars(CharSequence chars) {
    assert aligned() : "not aligned";
    incrementBitsWritten((long) chars.length() * Byte.SIZE);
    buffer.writeCharSequence(chars, CharsetUtil.US_ASCII);
    return this;
  }

  public ByteOutput writeChars(CharSequence chars, int len) {
    if (len < 0) throw new IllegalArgumentException("len(" + len + ") < " + 0);
    assert aligned() : "not aligned";
    final int charsLength = chars.length();
    if (len <= charsLength) {
      return writeChars(chars.subSequence(0, len));
    } else {
      return writeChars(chars).skipBytes(len - charsLength);
    }
  }

  public ByteOutput writeString(CharSequence chars) {
    return writeChars(chars).skipBytes(1);
  }

  // /**
  //  * @deprecated until fixed and null termination
  //  */
  // @Deprecated
  // public ByteOutput writeString(CharSequence chars, int len) {
  //   throw new UnsupportedOperationException("Not supported yet!");
  // }
}
