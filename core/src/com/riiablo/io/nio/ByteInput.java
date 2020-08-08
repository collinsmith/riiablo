package com.riiablo.io.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java.nio.charset.Charset;

import com.riiablo.util.DebugUtils;

/**
 * Wraps a {@link ByteBuf} to support reading sequences of bytes and supporting
 * {@link #unalign() unaligning} the byte stream to read sequences of
 * {@link BitInput bits}. All read functions will return results in little
 * endian byte order.
 *
 * @see BitInput
 * @see #unalign()
 */
// TODO: improve placeholder documentation
public class ByteInput {
  private static final ByteInput EMPTY_BYTEINPUT = new ByteInput(Unpooled.EMPTY_BUFFER);
  public static ByteInput emptyByteInput() {
    return EMPTY_BYTEINPUT;
  }

  public static ByteInput wrap(byte[] bytes) {
    return bytes == null ? emptyByteInput() : new ByteInput(Unpooled.wrappedBuffer(bytes));
  }

  private final ByteBuf buffer;
  private BitInput bitInput;

  ByteInput(ByteBuf buffer) {
    this.buffer = buffer;
  }

  /**
   * Returns a reference to the bit stream associated with this byte stream,
   * initializing one if it doesn't already exist.
   *
   * @see #unalign()
   */
  BitInput bitInput() {
    return bitInput != null ? bitInput : (bitInput = new BitInput(this));
  }

  /**
   * Assigns the bit stream of this byte stream to the specified bit stream.
   * <b>Precondition:</b> {@link #bitInput} is {@code null} and
   * {@link BitInput#byteInput()} is {@code this}.
   */
  BitInput bitInput(BitInput bitInput) {
    assert this.bitInput == null : "this.bitInput(" + this.bitInput + ") != null";
    assert bitInput.byteInput() == this : "bitInput.byteInput()(" + bitInput.byteInput() + ") != this(" + this + ")";
    return this.bitInput = bitInput;
  }

  public int bytesRead() {
    return buffer.readerIndex();
  }

  public int bytesRemaining() {
    return buffer.readableBytes();
  }

  public int numBytes() {
    return buffer.capacity();
  }

  /**
   * Indicates whether or not this byte stream's current bit is located on a
   * byte boundary. This method takes into account the read position of the
   * {@link #unalign() child bit stream} in order to enforce invariants.
   */
  public boolean aligned() {
    return bitInput == null || bitInput.aligned();
  }

  /**
   * Returns a byte unaligned view of this byte stream's content. This method
   * should be called when byte unaligned operations are required. Returning to
   * the byte stream state can be done via {@link BitInput#align()}.
   *
   * @see BitInput#align()
   * @see #bitInput()
   */
  public BitInput unalign() {
    return bitInput();
  }

  /**
   * Skips <i>n</i> bytes by discarding them.
   */
  public ByteInput skipBytes(int bytes) {
    incrementBitsRead((long) bytes * Byte.SIZE);
    buffer.skipBytes(bytes);
    return this;
  }

  /**
   * Consumes bytes until the specified sequence of bytes are encountered.
   * After this method executes, position will be such that the next read
   * operation is at the first byte in the signature.
   * <p/>
   * <b>Precondition:</b> {@code signature.length == 2}.
   *
   * @see #skipBytes(int)
   */
  public ByteInput skipUntil(byte[] signature) {
    assert aligned() : "not aligned";
    if (signature.length != 2) {
      throw new IllegalArgumentException(
          "signature.length(" + signature.length + ") != " + 2 + ": " + DebugUtils.toByteArray(signature));
    }

    final byte fb0 = signature[0];
    final byte fb1 = signature[1];
    byte b0, b1;
    b1 = read8();
    for (;;) {
      b0 = b1;
      b1 = read8();
      if (b0 == fb0 && b1 == fb1) {
        buffer.readerIndex(buffer.readerIndex() - signature.length);
        decrementBitsRead((long) signature.length * Byte.SIZE);
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
   * Reads a slice of this buffer's sub-region starting at the current position
   * and increases the position by the size of the new slice (= numBytes).
   *
   * @see ByteBuf#readSlice(int)
   */
  public ByteInput readSlice(long numBytes) {
    assert numBytes <= Integer.MAX_VALUE : "ByteBuf only supports int length";
    final ByteBuf slice = buffer.readSlice((int) numBytes);
    return new ByteInput(slice);
  }

  /**
   * Reads the next byte from the byte stream, ignoring alignment.
   */
  short _read8u() {
    try {
      final short octet = buffer.readUnsignedByte();
      assert 0 <= octet && octet < (1 << Byte.SIZE);
      return octet;
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  long incrementBitsRead(long bits) {
    assert (bits & (Byte.SIZE - 1)) == 0;
    if (bitInput == null) return 0;
    return bitInput.incrementBitsRead(bits);
  }

  long decrementBitsRead(long bits) {
    assert (bits & (Byte.SIZE - 1)) == 0;
    if (bitInput == null) return 0;
    return bitInput.decrementBitsRead(bits);
  }

  /**
   * Reads an unsigned byte.
   *
   * @see ByteBuf#readUnsignedByte()
   */
  public short read8u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Byte.SIZE);
      return buffer.readUnsignedByte();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads an unsigned 16-bit short integer.
   *
   * @see ByteBuf#readUnsignedShortLE()
   */
  public int read16u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Short.SIZE);
      return buffer.readUnsignedShortLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads an unsigned 32-bit integer.
   *
   * @see ByteBuf#readUnsignedIntLE()
   */
  public long read32u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Integer.SIZE);
      return buffer.readUnsignedIntLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a byte.
   *
   * @see ByteBuf#readByte()
   */
  public byte read8() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Byte.SIZE);
      return buffer.readByte();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 16-bit short integer.
   *
   * @see ByteBuf#readShortLE()
   */
  public short read16() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Short.SIZE);
      return buffer.readShortLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 32-bit integer.
   *
   * @see ByteBuf#readIntLE()
   */
  public int read32() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Integer.SIZE);
      return buffer.readIntLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads a 64-bit long integer.
   *
   * @see ByteBuf#readLongLE()
   */
  public long read64() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Long.SIZE);
      return buffer.readLongLE();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Transfers this buffer's data to a newly created byte array starting at the
   * current position and increases the position by the number of the
   * transferred bytes (= length).
   *
   * @see ByteBuf#readBytes(int)
   */
  public byte[] readBytes(int len) {
    return readBytes(new byte[len]);
  }

  /**
   * Transfers this buffer's data to a newly created byte array starting at the
   * current position and increases the position by the number of the
   * transferred bytes (= length).
   *
   * @see ByteBuf#readBytes(byte[])
   */
  public byte[] readBytes(byte[] dst) {
    return readBytes(dst, 0, dst.length);
  }

  /**
   * Transfers this buffer's data to a newly created byte array starting at the
   * current position and increases the position by the number of the
   * transferred bytes (= length).
   *
   * @see ByteBuf#readBytes(byte[], int, int)
   */
  public byte[] readBytes(byte[] dst, int dstOffset, int len) {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead((long) len * Byte.SIZE);
      buffer.readBytes(dst, dstOffset, len);
      return dst;
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }

  /**
   * Reads <i>n</i> bytes from the bit stream and constructs a string.
   *
   * @see ByteBuf#readCharSequence(int, Charset)
   */
  public String readString(int len) {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead((long) len * Byte.SIZE);
      return buffer.readCharSequence(len, CharsetUtil.US_ASCII).toString();
    } catch (IndexOutOfBoundsException t) {
      throw new EndOfInput(t);
    }
  }
}
