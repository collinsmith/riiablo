package com.riiablo.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;

import com.riiablo.util.DebugUtils;

public class BitInput {
  private static final BitInput EMPTY_BITINPUT = new BitInput(new byte[0]);
  public static BitInput emptyBitInput() {
    return EMPTY_BITINPUT;
  }

  public static BitInput wrap(byte[] bytes) {
    return bytes == null ? emptyBitInput() : new BitInput(bytes);
  }

  private static final int MAX_ULONG_BITS = Long.SIZE - 1;
  private static final int MAX_UINT_BITS = Integer.SIZE - 1;
  private static final int MAX_USHORT_BITS = Short.SIZE - 1;
  private static final int MAX_UBYTE_BITS = Byte.SIZE - 1;
  private static final int MAX_UNSIGNED_BITS = MAX_ULONG_BITS;

  private static final int MAX_SAFE_CACHED_BITS = Long.SIZE - Byte.SIZE;

  private static final long[] MASKS = new long[Long.SIZE];
  static {
    for (int i = 1; i < Long.SIZE; i++) {
      MASKS[i] = (MASKS[i - 1] << 1) + 1;
    }
  }

  private final ByteBuf buffer;
  private final long numBits;
  private long bitsRead;
  private int bitsCached;
  private long cache;

  private BitInput(byte[] b) {
    buffer = Unpooled.wrappedBuffer(b);
    numBits = (long) b.length * Byte.SIZE;
  }

  /**
   * Clears the overflow bits of the previously read byte.
   */
  public void clearCache() {
    bitsCached = 0;
    cache = 0L;
  }

  int bitsCached() {
    return bitsCached;
  }

  long cache() {
    return cache;
  }

  public long bitsRead() {
    return bitsRead;
  }

  public long numBits() {
    return numBits;
  }

  // TODO: include parent offset?
  public int bytePosition() {
    return buffer.readerIndex();
  }

  public int bytesRemaining() {
    return buffer.readableBytes();
  }

  // TODO: include parent offset?
  public long bitPosition() {
    assert bitsRead == ((Byte.SIZE - bitsCached) + ((long) bytePosition() * Byte.SIZE))
        : "actual(" + bitsRead + ") != expected(" + ((Byte.SIZE - bitsCached) + ((long) bytePosition() * Byte.SIZE)) + ")";
    return bitsRead;
  }

  public long bitsRemaining() {
    assert (numBits - bitsRead) == (bitsCached + ((long) bytesRemaining() * Byte.SIZE))
        : "actual(" + (numBits - bitsRead) + ") != expected(" + (bitsCached + ((long) bytesRemaining() * Byte.SIZE)) + ")";
    return numBits - bitsRead;
  }

  /**
   * Skips <i>n</i> bits by discarding them.
   */
  public BitInput skip(long bits) {
    if (bits < 0) throw new IllegalArgumentException("bits(" + bits + ") < " + 0);
    if (bits == 0) return this;

    final long startingBitsRead = bitsRead;
    final long bytes = bits / Byte.SIZE;
    assert bytes <= Integer.MAX_VALUE : "bytes(" + bytes + ") > Integer.MAX_VALUE";
    if (bytes > 0) align((int) bytes);

    final long overflowBits = (startingBitsRead + bits) - bitsRead;
    // checks single byte, multi-byte and expected max value
    assert bytes != 0 || overflowBits < Byte.SIZE : "overflowBits(" + overflowBits + ") > " + (Byte.SIZE - 1);
    assert bytes == 0 || overflowBits < Short.SIZE : "overflowBits(" + overflowBits + ") > " + (Short.SIZE - 1);
    assert overflowBits < Short.SIZE : "overflowBits(" + overflowBits + ") > " + (Short.SIZE - 1);
    if (overflowBits > 0) _readRaw((int) overflowBits);
    return this;
  }

  /**
   * Aligns the bit stream to the nearest byte boundary, or not at all if it is
   * already at one.
   *
   * @see #align(int)
   */
  public BitInput align() {
    return align(0);
  }

  /**
   * Aligns the bit stream to the <i>nth</i> byte boundary. Alignment will
   * behave as follows:
   * <ul>
   *   <li>cache will always be erased</li>
   *   <li>{@code cached bits = 0} will consume {@code 0} bytes</li>
   *   <li>{@code cached bits > 0} will consume {@code 1} byte</li>
   * </ul>
   */
  public BitInput align(int bytes) {
    if (bytes < 0) throw new IllegalArgumentException("bytes(" + bytes + ") < " + 0);

    // consume cache if bits remaining
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    if (bitsCached > 0) {
      bitsRead = Math.min(numBits, bitsRead + bitsCached);
      if (bytes > 0) bytes--;
      clearCache();
    }

    if (bytes > 0) {
      bitsRead = Math.min(numBits, bitsRead + (bytes * Byte.SIZE));
      buffer.skipBytes(bytes);
    }

    assert bitsRead <= numBits : "bitsRead(" + bitsRead + ") > numBits(" + numBits + ")";
    return this;
  }

  /**
   * Returns whether or not the bit stream is aligned on a byte boundary.
   */
  public boolean isAligned() {
    assert bitsCached < Byte.SIZE : "bitsCached(" + bitsCached + ") > " + (Byte.SIZE - 1);
    return bitsCached == 0;
  }

  /**
   * Consumes bits until the specified sequence of bytes are encountered. After
   * this method executes, position will be such that the next read operation
   * is at the first byte in the signature. This method will align the bit
   * stream to the byte boundary according to {@link #align()}.
   * <p/>
   * <b>Precondition:</b> {@code signature.length == 2}.
   *
   * @see #align()
   * @see #skip(long)
   */
  public BitInput skipUntil(byte[] signature) {
    if (signature.length != 2) {
      throw new IllegalArgumentException(
          "signature.length(" + signature.length + ") != " + 2 + ": " + DebugUtils.toByteArray(signature));
    }

    align();
    final byte fb0 = signature[0];
    final byte fb1 = signature[1];
    byte b0, b1;
    b1 = read8();
    for (;;) {
      b0 = b1;
      b1 = read8();
      if (b0 == fb0 && b1 == fb1) {
        buffer.readerIndex(buffer.readerIndex() - signature.length);
        bitsRead -= ((long) signature.length * Byte.SIZE);
        assert bitsRead >= 0 : "bitsRead(" + bitsRead + ") < " + 0;
        assert bytesRemaining() >= signature.length
            : "bytesRemaining(" + bytesRemaining() + ") < signature.length(" + signature.length + ")";
        break;
      }
    }

    // TODO: support dynamic signature lengths
    //       create a byte[] of size signature.length
    //       use as a circular buffer with each read byte incrementing index and then going back to
    //       0 when signature.length is reached. Comparisons will need to be index..length
    //       and 0..index (and 0..length in case where index == 0)

    return this;
  }

  /**
   * Reads up to {@value #MAX_UBYTE_BITS} bits as unsigned and casts the result
   * into a {@code byte}.
   */
  public byte read7u(int bits) {
    BitConstraints.validate7u(bits);
    final byte value = (byte) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Byte.SIZE);
    return value;
  }

  /**
   * Reads up to {@value #MAX_USHORT_BITS} bits as unsigned and casts the
   * result into a {@code short}.
   */
  public short read15u(int bits) {
    BitConstraints.validate15u(bits);
    final short value = (short) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Short.SIZE);
    return value;
  }

  /**
   * Reads up to {@value #MAX_UINT_BITS} bits as unsigned and casts the result
   * into a {@code int}.
   */
  public int read31u(int bits) {
    BitConstraints.validate31u(bits);
    final int value = (int) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Integer.SIZE);
    return value;
  }

  /**
   * Reads up to {@value #MAX_ULONG_BITS} bits as unsigned and casts the result
   * into a {@code long}.
   */
  public long read63u(int bits) {
    BitConstraints.validate63u(bits);
    final long value = (long) readUnsigned(bits);
    assert BitConstraints.isUnsigned(value, Long.SIZE);
    return value;
  }

  /**
   * Reads {@code 1} bit as a {@code boolean}.
   */
  public boolean readBoolean() {
    return read1() != 0;
  }

  /**
   * Reads {@code 1} bit as a {@code byte}.
   */
  public byte read1() {
    final byte value = read7u(1);
    assert (value & ~1) == 0;
    return value;
  }

  /**
   * Reads up to {@value Byte#SIZE} bits as a sign-extended {@code byte}.
   */
  public byte read8(int bits) {
    BitConstraints.validate8(bits);
    return (byte) readSigned(bits);
  }

  /**
   * Reads up to {@value Short#SIZE} bits as a sign-extended {@code short}.
   */
  public short read16(int bits) {
    BitConstraints.validate16(bits);
    return (short) readSigned(bits);
  }

  /**
   * Reads up to {@value Integer#SIZE} bits as a sign-extended {@code int}.
   */
  public int read32(int bits) {
    BitConstraints.validate32(bits);
    return (int) readSigned(bits);
  }

  /**
   * Reads up to {@value Long#SIZE} bits as a sign-extended {@code long}.
   */
  public long read64(int bits) {
    BitConstraints.validate64(bits);
    return readSigned(bits);
  }

  /**
   * Reads up to {@value Long#SIZE} bits as a {@code long}. This method is
   * intended to be used to read raw memory (i.e., flags).
   */
  public long readRaw(int bits) {
    BitConstraints.validate64(bits);
    return _readRaw(bits);
  }

  /**
   * @see #readRaw
   */
  private long _readRaw(int bits) {
    assert bits > 0 : "bits(" + bits + ") <= " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    long lo = readUnsigned(Math.min(Integer.SIZE, bits));
    long hi = readUnsigned(Math.max(bits - Integer.SIZE, 0));
    return (hi << Integer.SIZE) | lo;
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

  /**
   * Reads the next byte from the underlying byte stream, ignoring alignment.
   */
  private short _read8u() {
    try {
      final short octet = buffer.readUnsignedByte();
      assert 0 <= octet && octet < (1 << Byte.SIZE);
      return octet;
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  long incrementBitsRead(long bits) {
    if ((bitsRead += bits) > numBits) {
      bitsRead = bits;
      throw new EndOfInput();
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
      final long nextByte = _read8u();
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
    final long nextByte = _read8u();
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

  private void validateAlignment() {
    if (!isAligned()) {
      throw new IllegalStateException(
          "no-args method called on unaligned stream!");
    }
  }

  /**
   * Reads an unsigned byte from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public short read8u() {
    validateAlignment();
    try {
      incrementBitsRead(Byte.SIZE);
      return buffer.readUnsignedByte();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads an unsigned 16-bit short integer from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public int read16u() {
    validateAlignment();
    try {
      incrementBitsRead(Short.SIZE);
      return buffer.readUnsignedShortLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads an unsigned 32-bit integer from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public long read32u() {
    validateAlignment();
    try {
      incrementBitsRead(Integer.SIZE);
      return buffer.readUnsignedIntLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a byte from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public byte read8() {
    validateAlignment();
    try {
      incrementBitsRead(Byte.SIZE);
      return buffer.readByte();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 16-bit short integer from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public short read16() {
    validateAlignment();
    try {
      incrementBitsRead(Short.SIZE);
      return buffer.readShortLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 32-bit integer from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public int read32() {
    validateAlignment();
    try {
      incrementBitsRead(Integer.SIZE);
      return buffer.readIntLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 64-bit long integer from the bit stream.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   */
  public long read64() {
    validateAlignment();
    try {
      incrementBitsRead(Long.SIZE);
      return buffer.readLongLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads <i>n</i> bytes from the bit stream into a created byte array.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   *
   * @see #readBytes(byte[])
   * @see #readBytes(byte[], int, int)
   */
  public byte[] readBytes(int len) {
    validateAlignment();
    byte[] dst = new byte[len];
    readBytes(dst);
    return dst;
  }

  /**
   * Reads <i>n</i> bytes from the bit stream into the specified byte array.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   *
   * @see #readBytes(int)
   * @see #readBytes(byte[], int, int)
   */
  public void readBytes(byte[] dst) {
    validateAlignment();
    readBytes(dst, 0, dst.length);
  }

  /**
   * Reads <i>n</i> bytes from the bit stream into the specified byte array.
   * <p/>
   * <b>Precondition:</b> This stream must be byte aligned to use this method.
   *
   * @see #readBytes(int)
   * @see #readBytes(byte[])
   */
  public void readBytes(byte[] dst, int dstOffset, int len) {
    validateAlignment();
    align();
    assert bitsCached == 0;
    incrementBitsRead((long) len * Byte.SIZE);
    buffer.readBytes(dst, dstOffset, len);
  }

  /**
   * Reads <i>n</i> bytes from the bit stream and constructs a String.
   */
  public String readString(int len) {
    return readString(len, Byte.SIZE, false);
  }

  /**
   * Reads <i>n</i> characters of size {@code bits} and constructs a String.
   *
   * @param len number of characters to read
   * @param bits size of each character ({@code 7} or {@code 8})
   * @param nullTerminate {@code true} to stop reading at {@code \0}, otherwise
   *     {@code len} characters will be read
   */
  public String readString(int len, int bits, boolean nullTerminate) {
    if (len < 0) throw new IllegalArgumentException("len(" + len + ") < " + 0);
    BitConstraints.validateAscii(bits);

    if (len == 0) return StringUtils.EMPTY;
    final byte[] dst = new byte[len];
    for (int i = 0; i < len; i++) {
      final byte b = dst[i] = (byte) readUnsigned(bits);
      if (nullTerminate && b == '\0') break;
    }

    return new String(dst);
  }
}
