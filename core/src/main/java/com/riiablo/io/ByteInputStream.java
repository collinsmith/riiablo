package com.riiablo.io;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.input.SwappedDataInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static com.riiablo.util.ImplUtils.unsupported;

public class ByteInputStream {
  public static ByteInputStream wrap(InputStream in) {
    return wrap(in, 0);
  }

  public static ByteInputStream wrap(InputStream in, int offset) {
    return wrap(in, 0, Integer.MAX_VALUE);
  }

  public static ByteInputStream wrap(InputStream in, int offset, int length) {
    return new ByteInputStream(in, offset, length);
  }

  private final DataInput in;
  private final int offset;
  private final int length;
  private int readerIndex;
  private int mark;

  ByteInputStream(InputStream in, int offset, int length) {
    this.in = new SwappedDataInputStream(in);
    this.offset = offset;
    this.length = length;
    updateMark();
  }

  public void resetMark() {
    readerIndex = 0;
  }

  public int updateMark() {
    return mark = offset + readerIndex;
  }

  public int mark() {
    return mark;
  }

  public int bytesRead() {
    return readerIndex;
  }

  public int bytesRemaining() {
    return length - readerIndex;
  }

  public boolean aligned() {
    return true;
  }

  public ByteInputStream skipBytes(int bytes) {
    incrementBitsRead((long) bytes * Byte.SIZE);
    try {
      /* keeps skipping chunks until length or eof reached, whichever first */
      int totalSkipped = 0, skipped;
      while ((totalSkipped += (skipped = in.skipBytes(bytes - totalSkipped))) < bytes && skipped > 0);
      if (totalSkipped < bytes) throw new EOFException();
      return this;
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public ByteInputStream skipUntil(byte[] signature) {
    return unsupported("not supported for input streams");
  }

  public ByteInputStream readSignature(byte[] signature) {
    return unsupported("not supported for input streams");
  }

  public ByteInputStream readSlice(long numBytes) {
    return unsupported("not supported for input streams");
  }

  public ByteInputStream slice(long numBytes) {
    return unsupported("not supported for input streams");
  }

  @Deprecated
  public byte[] duplicate(int offset, int len) {
    return unsupported("not supported for input streams");
  }

  int _read8u() {
    return unsupported("not supported for input streams");
  }

  long incrementBitsRead(long bits) {
    assert (((int) bits) & 0x7) == 0 : "not aligned";
    updateMark();
    readerIndex += (int) (bits >> 3L);
    return 0;
  }

  long decrementBitsRead(long bits) {
    return unsupported("not supported for input streams");
  }

  public short read8u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Byte.SIZE);
      return (short) in.readUnsignedByte();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public int read16u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Short.SIZE);
      return in.readUnsignedShort();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public long read32u() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Integer.SIZE);
      return in.readInt() & 0xFFFFFFFFL;
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public boolean readBoolean() {
    return read8() != 0;
  }

  public byte read8() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Byte.SIZE);
      return in.readByte();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public short read16() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Short.SIZE);
      return in.readShort();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public int read32() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Integer.SIZE);
      return in.readInt();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public long read64() {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead(Long.SIZE);
      return in.readLong();
    } catch (EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public byte readSafe8u() {
    assert aligned() : "not aligned";
    final short value = read8u(); // increments bits
    return BitConstraints.safe8u(mark, value);
  }

  public short readSafe16u() {
    assert aligned() : "not aligned";
    final int value = read16u(); // increments bits
    return BitConstraints.safe16u(mark, value);
  }

  public int readSafe32u() {
    assert aligned() : "not aligned";
    final long value = read32u(); // increments bits
    return BitConstraints.safe32u(mark, value);
  }

  public long readSafe64u() {
    assert aligned() : "not aligned";
    final long value = read64(); // increments bits
    return BitConstraints.safe64u(mark, value);
  }

  public byte[] readBytes(int len) {
    return readBytes(new byte[len]);
  }

  public byte[] readBytes(byte[] dst) {
    return readBytes(dst, 0, dst.length);
  }

  public byte[] readBytes(byte[] dst, int dstOffset, int len) {
    assert aligned() : "not aligned";
    try {
      incrementBitsRead((long) len * Byte.SIZE);
      in.readFully(dst, dstOffset, len);
      return dst;
    } catch (IndexOutOfBoundsException | EOFException t) {
      throw new EndOfInput(t);
    } catch (IOException t) {
      return ExceptionUtils.rethrow(t);
    }
  }

  public String readString(int len) {
    return unsupported("not supported for input streams");
  }

  public String readString(int maxLen, boolean nullTerminated) {
    return unsupported("not supported for input streams");
  }

  public String readString() {
    return unsupported("not supported for input streams");
  }
}
