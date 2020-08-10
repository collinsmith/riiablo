package com.riiablo.io;

public class BitOutput {
  private static final long[] MASKS = BitConstants.UNSIGNED_MASKS;

  private final boolean autoFlush = true;
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

  void align() {

  }

  void flush(boolean align) {
    // write cache and clear it (align stream too?)
    if (align) {
      align();
      // assert cache cleared
    } else {
      // write byte
      // backup writer index 1 byte
    }

    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    byteOutput._write8(cache);
    clearCache();
  }

  void _writeUnsigned(long value, int bits) {
    assert bits > 0 : "bits(" + bits + ") <= " + 0;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    assert (value & ~MASKS[bits]) == 0 : "value(" + value + ") is larger than bits(" + bits + ")";
    incrementBitsWritten(bits);
    cache |= (value << bitsCached);
    bitsCached += bits;
    if (bitsCached > Long.SIZE) {
      writeCacheSafe(value, bits);
    } else {
      writeCacheUnsafe(bits);
    }
  }

  private void writeCacheSafe(long value, int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    assert bitsCached > Long.SIZE : "bitsCached(" + bitsCached + ") <= " + Long.SIZE;
    assert bitsCached - bits < Byte.SIZE : "bitsCached(" + bitsCached + ") - bits(" + bits + ") > " + (Byte.SIZE - 1);

    final int overflowBits = bitsCached - Long.SIZE;
    final int overflowShift = Long.SIZE - overflowBits;

    for (; bitsCached >= Byte.SIZE; bitsCached -= Byte.SIZE) {
      final long octet = cache & 0xFF;
      byteOutput._write8(octet);
      cache >>>= Byte.SIZE;
    }

    assert overflowBits > 0 : "overflowBits(" + overflowBits + ") < " + 1;
    cache = (value >>> overflowShift) & MASKS[overflowBits];
  }

  private void writeCacheUnsafe(int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    assert bitsCached >= bits : "bitsCached(" + bitsCached + ") < bits(" + bits + ")";
    assert bitsCached <= Long.SIZE : "bitsCached(" + bitsCached + ") > " + Long.SIZE;
    for (; bitsCached >= Byte.SIZE; bitsCached -= Byte.SIZE) {
      final long octet = cache & 0xFF;
      byteOutput._write8(octet);
      cache >>>= Byte.SIZE;
    }
  }

  void writeSigned(long value, int bits) {
    assert bits > 0 : "bits(" + bits + ") <= " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    final int shift = Long.SIZE - bits;
    value = (value << shift >> shift) & MASKS[bits];
    _writeRaw(value, bits);
  }

  void _writeRaw(long value, int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    incrementBitsWritten(bits);
    cache |= (value << bitsCached);
    bitsCached += bits;

    final int overflowBits = bitsCached - Long.SIZE;
    _writeCache(bits);
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

  public void writeRaw(long value, int bits) {
    BitConstraints.validate64(bits);
  }

  public void writeUnsigned(long value, int bits) {
    write63u(value, bits);
  }

  public void write7u(byte value, int bits) {
    BitConstraints.validate7u(bits);
    _writeUnsigned(value, bits);
  }

  public void write15u(short value, int bits) {
    BitConstraints.validate15u(bits);
    _writeUnsigned(value, bits);
  }

  public void write31u(int value, int bits) {
    BitConstraints.validate31u(bits);
    _writeUnsigned(value, bits);
  }

  public void write63u(long value, int bits) {
    BitConstraints.validate63u(bits);
    _writeUnsigned(value, bits);
  }

  // sign-extends bits
  public void write8(int value, int bits) {
    BitConstraints.validate8(bits);
  }

  // sign-extends bits
  public void write16(int value, int bits) {
    BitConstraints.validate16(bits);
  }

  // sign-extends bits
  public void write32(int value, int bits) {
    BitConstraints.validate32(bits);
  }

  // sign-extends bits
  public void write64(long value, int bits) {
    BitConstraints.validate64(bits);
  }

  public void write8(int value) {
    write8(value, Byte.SIZE);
  }

  public void write16(int value) {
    write16(value, Short.SIZE);
  }

  public void write32(int value) {
    write32(value, Integer.SIZE);
  }

  public void write64(long value) {
    write64(value, Long.SIZE);
  }
}
