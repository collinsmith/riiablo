package com.riiablo.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

public class BitStream {
  private static final BitStream EMPTY_BITSTREAM = new BitStream(new byte[0]);
  public static BitStream emptyBitStream() {
    return EMPTY_BITSTREAM;
  }

  public static BitStream wrap(byte[] bytes) {
    return bytes == null ? emptyBitStream() : new BitStream(bytes);
  }

  /**
   * Maximum number of bits that are safe to represent an unsigned value with.
   */
  private static final int MAX_CACHE_SIZE = Long.SIZE - 1;
  private static final long[] MASKS = new long[MAX_CACHE_SIZE + 1];
  static {
    for (int i = 1; i <= MAX_CACHE_SIZE; i++) {
      MASKS[i] = (MASKS[i - 1] << 1) + 1;
    }
  }

  /**
   * Maximum number of bits a {@code long} can contain without overflowing when
   * performing a bitwise {@code <<} by {@value Byte#SIZE}.
   */
  private static final int MAX_SAFE_CACHE_SIZE = Long.SIZE - Byte.SIZE;

  /**
   * Expected size of the signature when calling {@link #skipUntil(byte[])}.
   * This value is hard-coded and fixed due to limitations, however the impl
   * may be changed in the future to support dynamic signature lengths. For
   * now this is sufficient for any needs related to this project.
   */
  // TODO: deprecate and support dynamic signature lengths
  private static final int SIGNATURE_SIZE = 2;

  /**
   * Parent of this bit stream, or {@code null} if it has none.
   */
  private final BitStream parent;

  /**
   * Buffer containing the byte stream to read bits from.
   */
  private final ByteBuf buffer;

  /**
   * Total number of bits within this bit stream. This value may within the
   * byte boundary of a byte.
   */
  private final long numBits;

  /**
   * Number of bits read from the underlying byte stream, not including
   * {@link #bitsCached}
   */
  private long bitsRead;

  /**
   * Number of bits within {@link #cache} that have been read from the
   * underlying byte stream.
   */
  private int bitsCached;

  /**
   * Sequence of bits from the underlying byte stream used to create a number
   * with the specified bits and store the overflow for the next read
   * operation.
   */
  private long cache;

  private BitStream(byte[] b) {
    parent = null;
    buffer = Unpooled.wrappedBuffer(b);
    numBits = (long) b.length * Byte.SIZE;
  }

  // TODO: how to manage skipping the subview?
  //       drop cache and skip to last byte
  //       set cache and bitsCached to last byte, or 0 if on boundary

  /**
   * Contains the logic of constructing a new bit stream as a slice of an
   * existing bit stream. This exists because it ensures that when the new bit
   * stream is constructed, any modifications to the parent bit stream will
   * have completed, rather than the alternative, where {@link #readSlice}
   * must construct the child and then move it's read position.
   *
   * @see #readSlice
   */
  private BitStream(BitStream parent, long numBits) {
    assert bitsCached < Byte.SIZE : "Expected bitsCached to be at most 7 bits, was: " + bitsCached;
    assert numBits > 0 : "Empty bit streams should use emptyBitStream() instead";
    assert parent.bitsRead + numBits <= parent.numBits : "numBits cannot exceed the number of bits remaining within the parent bit stream!";

    this.parent = parent;

    // length should include the last byte that bits belong (round to ceil)
    final long length = (numBits - parent.bitsCached + Byte.SIZE - 1) / Byte.SIZE;
    assert length <= Integer.MAX_VALUE : "ByteBuf only supports int";
    this.buffer = parent.buffer.slice(parent.buffer.readerIndex(), (int) length);
    this.numBits = numBits;

    cache = parent.cache;
    bitsCached = parent.bitsCached;
  }

  /**
   * Returns a slice of this bit stream's sub-region starting at the current
   * bit position.
   *
   * @see #readSlice
   */
  private BitStream slice(long numBits) {
    if (numBits <= 0) return emptyBitStream();
    Validate.isTrue(bitsRead + numBits <= this.numBits,
        "numBits cannot exceed the number of bits remaining within this bit stream! %d >= %d",
        bitsRead + numBits,
        this.numBits);
    return new BitStream(this, numBits);
  }

  /**
   * Returns a slice of this bit stream's sub-region starting at the current
   * bit position and increases the bit position of this bit stream by the size
   * of the new slice.
   *
   * @see #slice
   */
  public BitStream readSlice(long numBits) {
    BitStream slice = slice(numBits);
    if (numBits <= 0) return slice;

    // length should not include last byte in case of overflow -- skip will need to read this byte
    // in order to set cache properly (round to floor)
    final long length = (numBits - bitsCached) / Byte.SIZE;
    assert length <= Integer.MAX_VALUE : "ByteBuf only supports int";
    buffer.skipBytes((int) length);

    final int overflowBits = (int) ((bitsRead + numBits) % Byte.SIZE);
    bitsRead += (numBits - overflowBits);
    clearCache();
    skip(overflowBits);

    return slice;
  }

  /**
   * Returns a copy of the bytes backing this bit stream. This method is unsafe
   * and only provided temporarily.
   */
  @Deprecated
  public byte[] copyBytes() {
    if (!buffer.isReadable()) return ArrayUtils.EMPTY_BYTE_ARRAY;
    byte[] copy = new byte[buffer.capacity()];
    buffer.getBytes(0, copy);
    return copy;
  }

  /**
   * Clears the overflow bits of the previously read byte.
   */
  public void clearCache() {
    bitsCached = 0;
    cache = 0L;
  }

  /**
   * Returns the number of bits that have been read from the stream and stored
   * within {@link #cache()}.
   */
  public int bitsCached() {
    return bitsCached;
  }

  /**
   * Returns the contents of the cache used to construct numbers. After a read
   * operation completes, this will be filled with at most 7 bits.
   */
  public long cache() {
    return cache;
  }

  public int bytePosition() {
    return buffer.readerIndex() + (parent != null ? parent.bytePosition() : 0);
  }

  public int bytesAvailable() {
    return buffer.readableBytes();
  }

  /**
   * Returns the absolute bit position of this bit stream. This value includes
   * any offsets of any parent bit streams.
   *
   * @see #bitsRead()
   */
  public long bitPosition() {
    return bitsRead + (parent != null ? parent.bitPosition() : 0L);
  }

  public long bitsAvailable() {
    return bitsCached + ((long) bytesAvailable() * Byte.SIZE);
  }

  /**
   * Returns the number of bits read by this bit stream.
   *
   * @see #bitPosition
   */
  public long bitsRead() {
    return bitsRead;
  }

  /**
   * Returns the number of bits in this bit stream.
   */
  public long numBits() {
    return numBits;
  }

  /**
   * Skips up to 64 bits.
   */
  public BitStream skip(int bits) {
    readRaw(bits);
    return this;
  }

  /**
   * Skips bits remaining in currently processed byte.
   *
   * @see #skip
   */
  public BitStream alignToByte() {
    int bits = bitsCached % Byte.SIZE;
    if (bits > 0) skip(bits);
    return this;
  }

  /**
   * Consumes bits until the specified sequence of bytes are encountered.
   * After this function executes, position will be such that the next read
   * operation is at the first byte in the signature, or at the end of the
   * byte stream. This function will align the byte stream at the byte
   * boundary.
   * <p/>
   * NOTE: Only supports signatures of exactly {@value #SIGNATURE_SIZE} bytes.
   *
   * @see #alignToByte
   * @see #skip
   */
  // FIXME: is it expected behavior to allow throwing EndOfStream if no signature found?
  public BitStream skipUntil(byte[] signature) {
    Validate.isTrue(signature.length == SIGNATURE_SIZE, "Only supports signature length of " + SIGNATURE_SIZE);
    alignToByte();
    final byte fb0 = signature[0];
    final byte fb1 = signature[1];
    byte b0, b1;
    b1 = (byte) readUnsigned(Byte.SIZE);
    for (;;) {
      b0 = b1;
      b1 = (byte) readUnsigned(Byte.SIZE);
      if (b0 == fb0 && b1 == fb1) {
        buffer.readerIndex(buffer.readerIndex() - signature.length);
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
   * Reads up to 63 bits as unsigned and casts the result into a {@code long}.
   * {@link #readRaw} should be used if 64 bits need to be read, or the value
   * that is being read represents raw memory (i.e., flags).
   * <p/>
   * <p>{@code bits} should be between [0, {@value #MAX_CACHE_SIZE}].
   * <p>Reading {@code 0} bits will always return {@code 0}.
   *
   * @see #readRaw
   * @see #readSigned
   * @see #readU7
   * @see #readU15
   * @see #readU31
   * @see #readU63
   */
  public long readUnsigned(int bits) {
    Validate.inclusiveBetween(0, MAX_CACHE_SIZE, bits);
    if (bits == 0) return 0;
    if ((bitsRead += bits) > numBits) {
      bitsRead = numBits;
      throw new EndOfStream();
    }

    ensureCache(bits);
    return bitsCached < bits
        ? readCacheSafe(bits)
        : readCacheUnsafe(bits);
  }

  /**
   * Reads up to 7 bits as unsigned and casts the result into a {@code byte}.
   */
  public byte readU7(int bits) {
    Validate.isTrue(bits < Byte.SIZE, "only 7 bits can fit into byte and be unsigned. bits: " + bits);
    return (byte) readUnsigned(bits);
  }

  /**
   * Reads up to 15 bits as unsigned and casts the result into a {@code short}.
   */
  public short readU15(int bits) {
    Validate.isTrue(bits < Short.SIZE, "only 15 bits can fit into short and be unsigned. bits: " + bits);
    return (short) readUnsigned(bits);
  }

  /**
   * Reads up to 31 bits as unsigned and casts the result into a {@code int}.
   */
  public int readU31(int bits) {
    Validate.isTrue(bits < Integer.SIZE, "only 31 bits can fit into int and be unsigned. bits: " + bits);
    return (int) readUnsigned(bits);
  }

  /**
   * Reads up to 63 bits as unsigned and casts the result into a {@code long}.
   */
  public long readU63(int bits) {
    Validate.isTrue(bits < Long.SIZE, "only 63 bits can fit into long and be unsigned. bits: " + bits);
    return readUnsigned(bits);
  }

  /**
   * Reads up to 64 bits as a {@code long}. This function behaves identically
   * to {@link #readUnsigned}, with the exception that it is intended to be
   * used to read raw memory and support reading a 64 bit long, since it is
   * impossible to encode an unsigned 64-bit number as a {@code long}.
   */
  // TODO: there may be a better way to do this, but this is simple.
  public long readRaw(int bits) {
    Validate.inclusiveBetween(0, Long.SIZE, bits);
    long lo = readUnsigned(bits > Integer.SIZE ? Integer.SIZE : bits);
    long hi = readUnsigned(bits > Integer.SIZE ? bits - Integer.SIZE : 0);
    return (hi << Integer.SIZE) | lo;
  }

  /**
   * Reads up to 64 bits from the underlying byte stream, sign extending the
   * result as a {@code long}.
   *
   * @see #readUnsigned
   * @see #readRaw
   */
  public long readSigned(int bits) {
    if (bits == Long.SIZE) return readRaw(Long.SIZE);
    Validate.inclusiveBetween(0, MAX_CACHE_SIZE, bits);
    final int shift = Long.SIZE - bits;
    return readUnsigned(bits) << shift >> shift;
  }

  /**
   * Reads a single bit and casts the result into a {@code boolean}.
   */
  public boolean readBoolean() {
    return readUnsigned(1) == 1L;
  }

  /**
   * Reads a single bit and casts the result into a {@code byte}.
   */
  public byte readBit() {
    return (byte) readUnsigned(1);
  }

  /**
   * Reads <i>n</i> bytes from the underlying byte stream into the specified
   * array. This function will align the byte stream at the byte boundary
   * and clear the cache.
   *
   * @see #alignToByte
   * @see #clearCache
   * @see #read(byte[], int, int)
   */
  public void read(byte[] dst) {
    read(dst, 0, dst.length);
  }

  /**
   * Reads <i>n</i> bytes from the underlying byte stream into the specified
   * array. This function will align the byte stream at the byte boundary
   * and clear the cache.
   *
   * @see #alignToByte
   * @see #clearCache
   * @see #read(byte[])
   */
  public void read(byte[] dst, int dstOffset, int len) {
    alignToByte();
    clearCache();
    buffer.readBytes(dst, dstOffset, len);
  }

  /**
   * Reads <i>n</i> bytes from the underlying byte stream into a created byte
   * array. This function will align the byte stream at the byte boundary
   * and clear the cache.
   *
   * @see #alignToByte
   * @see #clearCache
   * @see #read(byte[])
   * @see #read(byte[], int, int)
   */
  public byte[] read(int len) {
    byte[] dst = new byte[len];
    read(dst);
    return dst;
  }

  /**
   * Reads <i>n</i> characters from the underlying byte stream, assuming each
   * character contains {@value Byte#SIZE} bits per character. This function
   * is guaranteed to read {@code len} characters.
   *
   * @see #readString(int, int)
   */
  public String readString(int len) {
    return readString(len, Byte.SIZE);
  }

  /**
   * Reads <i>n</i> characters from the underlying byte stream, assuming each
   * character contains {@code bitsPerChar} bits. This function is guaranteed
   * to read {@code len} characters.
   * <p/>
   * Note: This function does not support multi-byte character encoding,
   * therefore <pre>bitsPerChar <= {@value Byte#SIZE}</pre>
   *
   * @see #readString(int)
   */
  public String readString(int len, int bitsPerChar) {
    Validate.isTrue(len >= 0, "len must be positive!");
    Validate.inclusiveBetween(1, Byte.SIZE, bitsPerChar);
    return _readString(len, bitsPerChar, false);
  }

  /**
   * Reads up to <i>n</i> characters from the underlying byte stream, assuming
   * each character contains {@code bitsPerChar} bits. This function will stop
   * reading characters when a null-termination is encountered.
   * <p/>
   * Note: This function does not support multi-byte character encoding,
   * therefore <pre>bitsPerChar <= {@value Byte#SIZE}</pre>
   *
   * @see #readString(int, int)
   */
  public String readString0(int len, int bitsPerChar) {
    Validate.isTrue(len >= 0, "len must be positive!");
    Validate.inclusiveBetween(1, Byte.SIZE, bitsPerChar);
    return _readString(len, bitsPerChar, true);
  }

  private String _readString(int len, int bitsPerChar, boolean nullTerminate) {
    assert len >= 0 : "len must be positive!";
    assert bitsPerChar >= 1 && bitsPerChar <= Byte.SIZE : "bitsPerChar should be in (0,8]";
    byte[] b = new byte[len];
    for (int i = 0; i < len; i++) {
      b[i] = (byte) readUnsigned(bitsPerChar);
      if (nullTerminate && b[i] == '\0') break;
    }

    // TODO: Why is this done this roundabout way?
    return BufferUtils.readString(ByteBuffer.wrap(b), len);
  }

  private int readByte() {
    try {
      return buffer.readByte() & 0xFF;
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfStream();
    }
  }

  /**
   * Ensures {@link #cache} contains at least <i>n</i> bits, up to
   * {@value #MAX_SAFE_CACHE_SIZE} bits due to overflow.
   *
   * @throws EndOfStream if the underlying byte stream did not contain at least
   * <i>n</i> bits.
   */
  private int ensureCache(int bits) {
    while (bitsCached < bits && bitsCached <= MAX_SAFE_CACHE_SIZE) {
      final long nextByte = readByte();
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
    final int bitsToAddCount = bits - bitsCached;
    final int overflowBits = Byte.SIZE - bitsToAddCount;
    final long nextByte = readByte();
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
    final long bitsOut = cache & MASKS[bits];
    cache >>>= bits;
    bitsCached -= bits;
    return bitsOut;
  }

  public static class EndOfStream extends RuntimeException {
    EndOfStream() {
      super("The end of the stream has been reached!");
    }
  }
}
