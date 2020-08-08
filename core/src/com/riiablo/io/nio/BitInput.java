package com.riiablo.io.nio;

import org.apache.commons.lang3.StringUtils;

public class BitInput implements Aligned, AlignedReader, UnalignedReader {

  private static final int MAX_SAFE_CACHED_BITS = Long.SIZE - Byte.SIZE;

  private static final long[] MASKS = new long[Long.SIZE];
  static {
    for (int i = 1; i < Long.SIZE; i++) {
      MASKS[i] = (MASKS[i - 1] << 1) + 1;
    }
  }

  final ByteInput byteInput;
  final long numBits;
  long bitsRead;

  private int bitsCached;
  private long cache;

  BitInput(ByteInput byteInput) {
    this(byteInput, 0, 0L, (long) byteInput.bytesRemaining() * Byte.SIZE);
  }

  BitInput(ByteInput byteInput, int bitsCached, long cache, long numBits) {
    this.byteInput = byteInput;
    this.bitsCached = bitsCached;
    this.cache = cache;
    this.numBits = numBits;
  }

  @Override
  public int bytesRead() {
    return byteInput.bytesRead();
  }

  @Override
  public int bytesRemaining() {
    return byteInput.bytesRemaining();
  }

  @Override
  public int numBytes() {
    return byteInput.numBytes();
  }

  @Override
  public int bitsCached() {
    return bitsCached;
  }

  @Override
  public long cache() {
    return cache;
  }

  void clearCache() {
    bitsCached = 0;
    cache = 0L;
  }

  @Override
  public long bitsRead() {
    return bitsRead;
  }

  @Override
  public long bitsRemaining() {
    assert (numBits - bitsRead) == (bitsCached + ((long) bytesRemaining() * Byte.SIZE))
        : "actual(" + (numBits - bitsRead) + ") != expected(" + (bitsCached + ((long) bytesRemaining() * Byte.SIZE)) + ")";
    return numBits - bitsRead;
  }

  @Override
  public long numBits() {
    return numBits;
  }

  @Override
  public boolean aligned() {
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    return bitsCached == 0;
  }

  public ByteInput align() {
    // consume cache if bits remaining
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    if (bitsCached > 0) {
      bitsRead = Math.min(numBits, bitsRead + bitsCached);
      clearCache();
    }

    assert bitsRead <= numBits : "bitsRead(" + bitsRead + ") > numBits(" + numBits + ")";
    return byteInput;
  }

  public BitInput readSlice(long numBits) {
    // since this shouldn't go more than 1 level deep, can also generate a new
    // ByteInput with a new BitInput if allowing align
    if (numBits == 0) return ByteInput.emptyByteInput().unalign();
    if (numBits < 0) throw new IllegalArgumentException("numBits(" + numBits + ") < " + 0);
    if (bitsRead + numBits > this.numBits) {
      throw new IllegalArgumentException(
          "bitsRead(" + bitsRead + ") + sliceBits(" + numBits + ") > numBits(" + this.numBits + ")");
    }


    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);

    // length should include the last byte that bits belong (round to ceil)
    final long numBytes = (numBits - bitsCached + Byte.SIZE - 1) / Byte.SIZE;
    final ByteInput byteInput = this.byteInput.readSlice(numBytes);
    return byteInput.bitInput = new BitInput(byteInput, bitsCached, cache, numBits);
  }

  public BitInput discardBits(long bits) {
    if (bits < 0) throw new IllegalArgumentException("bits(" + bits + ") < " + 0);
    if (bits == 0) return this;

    final long startingBitsRead = bitsRead;
    final long bytes = bits / Byte.SIZE;
    assert bytes <= Integer.MAX_VALUE : "bytes(" + bytes + ") > Integer.MAX_VALUE";
    if (bytes > 0) align().discardBytes((int) bytes);

    final long overflowBits = (startingBitsRead + bits) - bitsRead;
    // checks single byte, multi-byte and expected max value
    assert bytes != 0 || overflowBits < Byte.SIZE : "overflowBits(" + overflowBits + ") > " + (Byte.SIZE - 1);
    assert bytes == 0 || overflowBits < Short.SIZE : "overflowBits(" + overflowBits + ") > " + (Short.SIZE - 1);
    assert overflowBits < Short.SIZE : "overflowBits(" + overflowBits + ") > " + (Short.SIZE - 1);
    if (overflowBits > 0) _readRaw((int) overflowBits);
    return this;
  }

  long incrementBitsRead(long bits) {
    if ((bitsRead += bits) > numBits) {
      bitsRead = numBits;
      throw new EndOfInput();
    }

    return bitsRead;
  }

  long decrementBitsRead(long bits) {
    if ((bitsRead -= bits) < 0) {
      assert false : "bitsRead(" + bitsRead + ") < " + 0;
      bitsRead = 0;
    }

    return bitsRead;
  }

  /**
   * Reads up to {@value #MAX_UNSIGNED_BITS} bits as unsigned and casts the
   * result into a {@code long}.
   * <p/>
   * <p>{@code bits} should be in [0, {@value #MAX_UNSIGNED_BITS}].
   * <p>Reading {@code 0} bits will always return {@code 0}.
   */
  long readUnsigned(int bits) {
    assert bits >= 0 : "bits(" + bits + ") < " + 0;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    if (bits <= 0) return 0;
    incrementBitsRead(bits);
    ensureCache(bits);
    return bitsCached < bits
        ? readCacheSafe(bits)
        : readCacheUnsafe(bits);
  }

  /**
   * Ensures {@link #cache} contains at least <i>n</i> bits, up to
   * {@value #MAX_SAFE_CACHED_BITS} bits due to overflow.
   */
  private int ensureCache(int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    while (bitsCached < bits && bitsCached <= MAX_SAFE_CACHED_BITS) {
      final long nextByte = byteInput._read8u();
      cache |= (nextByte << bitsCached);
      bitsCached += Byte.SIZE;
    }

    return bitsCached;
  }

  /**
   * Reads <i>n</i> bits from {@link #cache}, consuming the next byte in the
   * underlying byte stream.
   * <p/>
   * This function asserts that {@link #cache} would have overflowed if
   * <i>n</i> bits were read from the underlying byte stream and thus reads
   * the next byte accounting for this case.
   */
  private long readCacheSafe(int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    final int bitsToAddCount = bits - bitsCached;
    final int overflowBits = Byte.SIZE - bitsToAddCount;
    final long nextByte = byteInput._read8u();
    long bitsToAdd = nextByte & MASKS[bitsToAddCount];
    cache |= (bitsToAdd << bitsCached);
    final long overflow = (nextByte >>> bitsToAddCount) & MASKS[overflowBits];
    final long bitsOut = bitsCached & MASKS[bits];
    cache = overflow;
    bitsCached = overflowBits;
    return bitsOut;
  }

  /**
   * Reads <i>n</i> bits from {@link #cache}.
   * <p/>
   * This function asserts {@link #cache} contains at least <i>n</i> bits.
   */
  private long readCacheUnsafe(int bits) {
    assert bits > 0 : "bits(" + bits + ") < " + 1;
    assert bits < Long.SIZE : "bits(" + bits + ") > " + (Long.SIZE - 1);
    final long bitsOut = cache & MASKS[bits];
    cache >>>= bits;
    bitsCached -= bits;
    return bitsOut;
  }

  /**
   * Reads up to {@value Long#SIZE} bits and sign extending the result as a
   * {@code long}.
   * <p/>
   * <p>{@code bits} should be in [0, {@value Long#SIZE}].
   * <p>Reading {@code 0} bits will always return {@code 0}.
   */
  long readSigned(int bits) {
    assert bits >= 0 : "bits(" + bits + ") < " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    if (bits <= 0) return 0;
    if (bits == Long.SIZE) return _readRaw(Long.SIZE);
    final int shift = Long.SIZE - bits;
    assert shift > 0;
    final long value = readUnsigned(bits);
    return value << shift >> shift;
  }

  long _readRaw(int bits) {
    assert bits > 0 : "bits(" + bits + ") <= " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    long lo = readUnsigned(Math.min(Integer.SIZE, bits));
    long hi = readUnsigned(Math.max(bits - Integer.SIZE, 0));
    return (hi << Integer.SIZE) | lo;
  }

  @Override
  public long readRaw(int bits) {
    BitConstraints.validate64(bits);
    return _readRaw(bits);
  }

  @Override
  public byte read7u(int bits) {
    BitConstraints.validate7u(bits);
    final byte value = (byte) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Byte.SIZE);
    return value;
  }

  @Override
  public short read15u(int bits) {
    BitConstraints.validate15u(bits);
    final short value = (short) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Short.SIZE);
    return value;
  }

  @Override
  public int read31u(int bits) {
    BitConstraints.validate31u(bits);
    final int value = (int) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Integer.SIZE);
    return value;
  }

  @Override
  public long read63u(int bits) {
    BitConstraints.validate63u(bits);
    final long value = (long) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Long.SIZE);
    return value;
  }

  @Override
  public boolean readBoolean() {
    return read1() != 0;
  }

  @Override
  public byte read1() {
    final byte value = read7u(1);
    assert (value & ~1) == 0;
    return value;
  }

  @Override
  public byte read8(int bits) {
    BitConstraints.validate8(bits);
    return (byte) readSigned(bits);
  }

  @Override
  public short read16(int bits) {
    BitConstraints.validate16(bits);
    return (short) readSigned(bits);
  }

  @Override
  public int read32(int bits) {
    BitConstraints.validate32(bits);
    return (int) readSigned(bits);
  }

  @Override
  public long read64(int bits) {
    BitConstraints.validate64(bits);
    return readSigned(bits);
  }

  @Override
  public short read8u() {
    return read15u(Byte.SIZE);
  }

  @Override
  public int read16u() {
    return read31u(Short.SIZE);
  }

  @Override
  public long read32u() {
    return read63u(Integer.SIZE);
  }

  @Override
  public byte read8() {
    return read8(Byte.SIZE);
  }

  @Override
  public short read16() {
    return read16(Short.SIZE);
  }

  @Override
  public int read32() {
    return read32(Integer.SIZE);
  }

  @Override
  public long read64() {
    return read64(Long.SIZE);
  }

  /**
   * Aligns bit stream and reads from {@link #align()}
   */
  @Override
  public byte[] readBytes(int len) {
    return align().readBytes(len);
  }

  /**
   * Aligns bit stream and reads from {@link #align()}
   */
  @Override
  public byte[] readBytes(byte[] dst) {
    return align().readBytes(dst);
  }

  /**
   * Aligns bit stream and reads from {@link #align()}
   */
  @Override
  public byte[] readBytes(byte[] dst, int dstOffset, int len) {
    return align().readBytes(dst, dstOffset, len);
  }

  @Override
  public String readString(int len) {
    return readString(len, Byte.SIZE, false);
  }

  @Override
  public String readString(int len, int bits, boolean nullTerminated) {
    if (len < 0) throw new IllegalArgumentException("len(" + len + ") < " + 0);
    BitConstraints.validateAscii(bits);

    if (len == 0) return StringUtils.EMPTY;
    final byte[] dst = new byte[len];
    for (int i = 0; i < len; i++) {
      final byte b = dst[i] = (byte) readUnsigned(bits);
      if (nullTerminated && b == '\0') break;
    }

    return new String(dst);
  }
}
