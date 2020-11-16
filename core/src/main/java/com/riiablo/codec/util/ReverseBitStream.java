package com.riiablo.codec.util;

public class ReverseBitStream {
  public static final ReverseBitStream EMPTY_BITSTREAM = new ReverseBitStream();

  private static final int BYTE_MAX_POWER_OF_TWO = 1 << (Byte.SIZE - 1);

  byte buffer[];
  int  bufferLength;
  int  size;
  int  curBitPosition;

  private ReverseBitStream() {
    this(null, 0);
  }

  public ReverseBitStream(byte[] buffer, int sizeInBits) {
    this.buffer = buffer;
    this.bufferLength = buffer == null ? 0 : buffer.length - 1;
    this.size = sizeInBits;
    this.curBitPosition = 0;
  }

  public long tell() {
    return curBitPosition;
  }

  public long bufferSizeInBytes() {
    return (size + Byte.SIZE - 1) / Byte.SIZE;
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
    int curBytesPos     = (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (curBitPosition % Byte.SIZE);
    int mask = (BYTE_MAX_POWER_OF_TWO >>> bitPosInCurByte);
    curBitPosition++;
    return (buffer[bufferLength - curBytesPos] & mask) == mask;
  }

  public int readBit() {
    return readBoolean() ? 1 : 0;
  }

  public long readUnsigned(int bits) {
    if (bits == 0) return 0;
    int curBytesPos     = (curBitPosition / Byte.SIZE);
    int bitPosInCurByte = (curBitPosition % Byte.SIZE);
    long value = 0;
    curBitPosition += bits;
    for (int i = 0; i < bits;) {
      int bitsToReadInCurByte = Math.min(Byte.SIZE - bitPosInCurByte, bits - i);
      int mask = (BYTE_MAX_POWER_OF_TWO >>> bitsToReadInCurByte) - 1;
      int inBits = (buffer[bufferLength - curBytesPos] >> bitPosInCurByte) & mask;
      curBytesPos++;
      value |= (inBits << i);
      i += bitsToReadInCurByte;
      bitPosInCurByte = 0;
    }

    return value;
  }

  public int readUnsigned8OrLess(int bits) {
    assert bits <= Byte.SIZE;
    return (int) readUnsigned(bits);
  }

  public int read0Bits() {
    return 0;
  }

  public int readSigned(int bits) {
    int shift = Integer.SIZE - bits;
    return ((int) readUnsigned(bits)) << shift >> shift;
  }
}
