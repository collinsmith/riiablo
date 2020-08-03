package com.riiablo.codec.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;

import com.riiablo.util.BufferUtils;

public class BitStream {
  public static final BitStream EMPTY_BITSTREAM = new BitStream();

  byte[] buffer;
  int offset;
  long size;
  long firstBitOffset;
  long curBitPosition;

  private BitStream() {
    this(null, 0);
  }

  private BitStream(byte[] buffer, int offset, long sizeInBits, long firstBitOffsetInBuffer) {
    this.buffer = buffer;
    this.offset = offset;
    this.size = sizeInBits;
    this.firstBitOffset = this.curBitPosition = firstBitOffsetInBuffer;
  }

  public BitStream(byte[] buffer) {
    this(buffer, buffer.length << 3L);
  }

  public BitStream(byte[] buffer, long sizeInBits) {
    this(buffer, sizeInBits, 0);
  }

  public BitStream(byte[] buffer, long sizeInBits, long firstBitOffsetInBuffer) {
    this(buffer, 0, sizeInBits, firstBitOffsetInBuffer);
  }

  public BitStream createSubView(long newBufferSizeInBits) {
    if (newBufferSizeInBits == 0) return EMPTY_BITSTREAM;
    int curBytesPos     = (int) (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (int) (curBitPosition % Byte.SIZE);
    assert tell() + newBufferSizeInBits <= size;
    assert curBitPosition + newBufferSizeInBits <= bufferSizeInBits();
    return new BitStream(buffer, offset + curBytesPos, newBufferSizeInBits, bitPosInCurByte);
  }

  @Deprecated
  public int getOffset() {
    return offset;
  }

  @Deprecated
  public int getBytePosition() {
    return (int) (curBitPosition / Byte.SIZE);
  }

  @Deprecated
  public int getBitPosition() {
    return (int) (curBitPosition % Byte.SIZE);
  }

  @Deprecated
  public byte[] getBufferView() {
    return Arrays.copyOfRange(buffer, offset, buffer.length);
  }

  @Deprecated
  public byte[] getBufferAtPos() {
    int curBytesPos = (int) (curBitPosition / Byte.SIZE);
    return Arrays.copyOfRange(buffer, offset + curBytesPos, buffer.length);
  }

  public long tell() {
    return curBitPosition - firstBitOffset;
  }

  public long bufferSizeInBytes() {
    return (size + firstBitOffset + (Byte.SIZE - 1)) / Byte.SIZE;
  }

  public long bufferSizeInBits() {
    return bufferSizeInBytes() * Byte.SIZE;
  }

  public long sizeInBits() {
    return size;
  }

  public long bitPositionInBuffer() {
    return curBitPosition;
  }

  public void skip(long bits) {
    assert curBitPosition + bits < bufferSizeInBits();
    curBitPosition += bits;
  }

  public void alignToByte() {
    int highestBit = Byte.SIZE - 1;
    curBitPosition = (curBitPosition + highestBit) & (~highestBit);
  }

  public boolean readBoolean() {
    int curBytesPos     = (int) (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (int) (curBitPosition % Byte.SIZE);
    int mask = (1 << bitPosInCurByte);
    curBitPosition++;
    return (buffer[offset + curBytesPos] & mask) == mask;
  }

  public int readBit() {
    return readBoolean() ? 1 : 0;
  }

  public long readUnsigned(int bits) {
    Validate.inclusiveBetween(0, Long.SIZE, bits, "bits must be in range [0,64]");
    if (bits == 0) return 0;
    int curBytesPos     = (int) (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (int) (curBitPosition % Byte.SIZE);
    long value = 0;
    curBitPosition += bits;
    for (int i = 0; i < bits;) {
      int bitsToReadInCurByte = Math.min(Byte.SIZE - bitPosInCurByte, bits - i);
      int mask = (1 << bitsToReadInCurByte) - 1;
      int inBits = (buffer[offset + curBytesPos] >> bitPosInCurByte) & mask;
      curBytesPos++;
      value |= (inBits << i);
      i += bitsToReadInCurByte;
      bitPosInCurByte = 0;
    }

    return value;
  }

  public byte readU7(int bits) {
    assert bits < Byte.SIZE : "only 7 bits can fit into byte and be unsigned. bits: " + bits;
    return (byte) readUnsigned(bits);
  }

  public short readU15(int bits) {
    assert bits < Short.SIZE : "only 15 bits can fit into short and be unsigned. bits: " + bits;
    return (short) readUnsigned(bits);
  }

  public int readU31(int bits) {
    assert bits < Integer.SIZE : "only 31 bits can fit into int and be unsigned. bits: " + bits;
    return (int) readUnsigned(bits);
  }

  public long readU63(int bits) {
    assert bits < Long.SIZE : "only 63 bits can fit into long and be unsigned. bits: " + bits;
    return readUnsigned(bits);
  }

  public int readU8(int bits) {
    assert bits <= Byte.SIZE;
    return (int) readUnsigned(bits);
  }

  public long readSigned(int bits) {
    final int shift = Long.SIZE - bits;
    return readUnsigned(bits) << shift >> shift;
  }

  public void readFully(byte[] b) {
    assert sizeInBits() - tell() >= b.length * Byte.SIZE;
    int curBytesPos     = (int) (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (int) (curBitPosition % Byte.SIZE);
    if (bitPosInCurByte != 0) {
      alignToByte();
    }

    curBitPosition += (b.length * Byte.SIZE);
    System.arraycopy(buffer, offset + curBytesPos, b, 0, b.length);
  }

  public byte[] readFully(int size) {
    byte[] b = new byte[size];
    readFully(b);
    return b;
  }

  public String readString(int len) {
    return readString(len, Byte.SIZE);
  }

  public String readString(int len, int bitsPerChar) {
    assert bitsPerChar <= Byte.SIZE;
    byte[] b = new byte[len];
    for (int i = 0; i < len; i++) {
      b[i] = (byte) readU8(bitsPerChar);
    }
    return BufferUtils.readString(ByteBuffer.wrap(b), len);
  }

  public String readString2(int len, int bitsPerChar) {
    assert bitsPerChar <= Byte.SIZE;
    byte[] b = new byte[len];
    for (int i = 0; i < len && (b[i] = (byte) readU8(bitsPerChar)) != '\0'; i++);
    return BufferUtils.readString(ByteBuffer.wrap(b), len);
  }
}
