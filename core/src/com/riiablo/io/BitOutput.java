package com.riiablo.io;

public class BitOutput {
  private static final long[] MASKS = BitConstants.UNSIGNED_MASKS;

  private final ByteOutput byteOutput;
  private final long numBits;
  private long bitsWritten;
  private int bitsCached;
  private long cache;

  BitOutput(ByteOutput byteOutput) {
    this(byteOutput, 0, 0L, (long) byteOutput.bytesRemaining() * Byte.SIZE);
  }

  BitOutput(ByteOutput byteOutput, int bitsCached, long cache, long numBits) {
    this.byteOutput = byteOutput;
    this.bitsCached = bitsCached;
    this.cache = cache;
    this.numBits = numBits;
  }

  ByteOutput byteOutput() {
    return byteOutput;
  }

  int bitsCached() {
    return bitsCached;
  }

  long cache() {
    return cache;
  }

  void clearCache() {
    bitsCached = 0;
    cache = 0L;
  }

  long incrementBitsWritten(long bits) {
    if ((bitsWritten += bits) > numBits) {
      bitsWritten = numBits;
      throw new EndOfInput();
    }

    return bitsWritten;
  }

  public boolean aligned() {
    return true;
  }

  public ByteOutput align() {
    return flush().byteOutput;
  }

  public BitOutput flush() {
    assert bitsCached >= 0 : "bitsCached(" + bitsCached + ") < " + 0;
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    if (bitsCached <= 0) return this;
    byteOutput._write8(cache);
    clearCache();
    return this;
  }

  public BitOutput skipBits(long bits) {
    if (bits < 0) throw new IllegalArgumentException("bits(" + bits + ") < " + 0);
    if (bits == 0) return this;

    // aligns bit stream for multi-byte skipping
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    final int bitsUncached = Byte.SIZE - bitsCached;
    if (bits >= bitsUncached) {
      bits -= bitsUncached;
      flush();
    }

    // skips multiple bytes
    final long startingBitsWritten = bitsWritten;
    final long bytes = bits / Byte.SIZE;
    assert bytes <= Integer.MAX_VALUE : "bytes(" + bytes + ") > Integer.MAX_VALUE";
    if (bytes > 0) align().skipBytes((int) bytes);

    // skips overflow bits
    final long overflowBits = (startingBitsWritten + bits) - bitsWritten;
    assert overflowBits < Byte.SIZE : "overflowBits(" + overflowBits + ") > " + (Byte.SIZE - 1);
    if (overflowBits > 0) _writeRaw(0L, (int) overflowBits);
    return this;
  }

  void _writeUnsigned(long value, int bits) {
    assert bits >= 0 : "bits(" + bits + ") < " + 0;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    assert bits == 0 || (value & ~MASKS[bits]) == 0 : "value(" + value + ") is larger than bits(" + bits + ")";
    _writeRaw(value, bits);
  }

  void _writeRaw(long value, int bits) {
    assert bits >= 0 : "bits(" + bits + ") < " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    if (bits == 0) return;
    incrementBitsWritten(bits);
    cache |= (value << bitsCached);
    bitsCached += bits;

    final int overflowBits = bitsCached - Long.SIZE;
    _writeCache(bits);
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    if (overflowBits > 0) {
      final int overflowShift = Long.SIZE - overflowBits;
      cache = (value >>> overflowShift) & MASKS[overflowBits];
    }
  }

  private void _writeCache(int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    assert bitsCached >= bits : "bitsCached(" + bitsCached + ") < bits(" + bits + ")";
    assert bitsCached <= (Long.SIZE + Byte.SIZE - 1) : "bitsCached(" + bitsCached + ") > " + (Long.SIZE + Byte.SIZE - 1);
    for (; bitsCached >= Byte.SIZE; bitsCached -= Byte.SIZE) {
      final long octet = cache & 0xFF;
      byteOutput._write8(octet);
      cache >>>= Byte.SIZE;
    }
  }

  void _writeSigned(long value, int bits) {
    assert bits > 0 : "bits(" + bits + ") <= " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    final int shift = Long.SIZE - bits;
    value = (value << shift >> shift) & MASKS[bits];
    _writeRaw(value, bits);
  }

  public BitOutput writeRaw(long value, int bits) {
    BitConstraints.validate64(bits);
    _writeRaw(value, bits);
    return this;
  }

  public BitOutput write7u(int value, int bits) {
    BitConstraints.validate7u(bits);
    _writeUnsigned(value, bits);
    return this;
  }

  public BitOutput write15u(int value, int bits) {
    BitConstraints.validate15u(bits);
    _writeUnsigned(value, bits);
    return this;
  }

  public BitOutput write31u(int value, int bits) {
    BitConstraints.validate31u(bits);
    _writeUnsigned(value, bits);
    return this;
  }

  public BitOutput write63u(long value, int bits) {
    BitConstraints.validate63u(bits);
    _writeUnsigned(value, bits);
    return this;
  }

  public BitOutput writeBoolean(boolean b) {
    return write1(b ? 1 : 0);
  }

  public BitOutput write1(int value) {
    _writeRaw(value, 1);
    return this;
  }

  public BitOutput write8(int value, int bits) {
    BitConstraints.validate8(bits);
    _writeSigned(value, bits);
    return this;
  }

  public BitOutput write16(int value, int bits) {
    BitConstraints.validate16(bits);
    _writeSigned(value, bits);
    return this;
  }

  public BitOutput write32(int value, int bits) {
    BitConstraints.validate32(bits);
    _writeSigned(value, bits);
    return this;
  }

  public BitOutput write64(long value, int bits) {
    BitConstraints.validate64(bits);
    _writeSigned(value, bits);
    return this;
  }

  public BitOutput write8(int value) {
    write8(value, Byte.SIZE);
    return this;
  }

  public BitOutput write16(int value) {
    write16(value, Short.SIZE);
    return this;
  }

  public BitOutput write32(int value) {
    write32(value, Integer.SIZE);
    return this;
  }

  public BitOutput write64(long value) {
    write64(value, Long.SIZE);
    return this;
  }

  public BitOutput writeString(CharSequence chars, int bits) {
    return writeString(chars, bits, false);
  }

  public BitOutput writeString(CharSequence chars, int bits, boolean nullTerminated) {
    BitConstraints.validateAscii(bits);

    final int length = chars.length();
    if (length == 0) return this;
    for (int i = 0; i < length; i++) {
      final char c = chars.charAt(i);
      _writeRaw(c, bits);
    }

    if (nullTerminated) _writeRaw('\0', bits);
    return this;
  }
}
