/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.riiablo.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * An implementation of {@link DataInput} that uses little-endian byte ordering for reading {@code
 * short}, {@code int}, {@code float}, {@code double}, and {@code long} values.
 *
 * <p><b>Note:</b> This class intentionally violates the specification of its supertype {@code
 * DataInput}, which explicitly requires big-endian byte order.
 *
 * <p><b>Note:</b> Copied from https://github.com/google/guava fd919e54a55ba169dc7d9f54b7b3485aa7fa0970
 * @deprecated To be replaced with com.riiablo.io implementation
 *
 * @author Chris Nokleberg
 * @author Keith Bottner
 */
@Deprecated
public final class LittleEndianDataInputStream extends FilterInputStream implements DataInput {

  /**
   * Creates a {@code LittleEndianDataInputStream} that wraps the given stream.
   *
   * @param in the stream to delegate to
   */
  public LittleEndianDataInputStream(InputStream in) {
    super(Validate.notNull(in));
  }

  /** This method will throw an {@link UnsupportedOperationException}. */
  @Override
  public String readLine() {
    throw new UnsupportedOperationException("readLine is not supported");
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    IOUtils.readFully(this, b);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    IOUtils.readFully(this, b, off, len);
  }

  @Override
  public int skipBytes(int n) throws IOException {
    return (int) in.skip(n);
  }

  @Override
  public int readUnsignedByte() throws IOException {
    int b1 = in.read();
    if (0 > b1) {
      throw new EOFException();
    }

    return b1;
  }

  /**
   * Returns the {@code int} value whose byte representation is the given 4 bytes, in big-endian
   * order.
   *
   * Copied from com.google.common.primitives.Ints
   */
  private static int intFromBytes(byte b1, byte b2, byte b3, byte b4) {
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
  }

  /**
   * Reads an unsigned {@code short} as specified by {@link DataInputStream#readUnsignedShort()},
   * except using little-endian byte order.
   *
   * @return the next two bytes of the input stream, interpreted as an unsigned 16-bit integer in
   *     little-endian byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public int readUnsignedShort() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();

    return intFromBytes((byte) 0, (byte) 0, b2, b1);
  }

  /**
   * Reads an integer as specified by {@link DataInputStream#readInt()}, except using little-endian
   * byte order.
   *
   * @return the next four bytes of the input stream, interpreted as an {@code int} in little-endian
   *     byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public int readInt() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();
    byte b3 = readAndCheckByte();
    byte b4 = readAndCheckByte();

    return intFromBytes(b4, b3, b2, b1);
  }

  /**
   * Returns the {@code long} value whose byte representation is the given 8 bytes, in big-endian
   * order.
   *
   * Copied from com.google.common.primitives.Longs
   */
  public static long longFromBytes(
      byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
    return (b1 & 0xFFL) << 56
        | (b2 & 0xFFL) << 48
        | (b3 & 0xFFL) << 40
        | (b4 & 0xFFL) << 32
        | (b5 & 0xFFL) << 24
        | (b6 & 0xFFL) << 16
        | (b7 & 0xFFL) << 8
        | (b8 & 0xFFL);
  }

  /**
   * Reads a {@code long} as specified by {@link DataInputStream#readLong()}, except using
   * little-endian byte order.
   *
   * @return the next eight bytes of the input stream, interpreted as a {@code long} in
   *     little-endian byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public long readLong() throws IOException {
    byte b1 = readAndCheckByte();
    byte b2 = readAndCheckByte();
    byte b3 = readAndCheckByte();
    byte b4 = readAndCheckByte();
    byte b5 = readAndCheckByte();
    byte b6 = readAndCheckByte();
    byte b7 = readAndCheckByte();
    byte b8 = readAndCheckByte();

    return longFromBytes(b8, b7, b6, b5, b4, b3, b2, b1);
  }

  /**
   * Reads a {@code float} as specified by {@link DataInputStream#readFloat()}, except using
   * little-endian byte order.
   *
   * @return the next four bytes of the input stream, interpreted as a {@code float} in
   *     little-endian byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * Reads a {@code double} as specified by {@link DataInputStream#readDouble()}, except using
   * little-endian byte order.
   *
   * @return the next eight bytes of the input stream, interpreted as a {@code double} in
   *     little-endian byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  @Override
  public String readUTF() throws IOException {
    return new DataInputStream(in).readUTF();
  }

  /**
   * Reads a {@code short} as specified by {@link DataInputStream#readShort()}, except using
   * little-endian byte order.
   *
   * @return the next two bytes of the input stream, interpreted as a {@code short} in little-endian
   *     byte order.
   * @throws IOException if an I/O error occurs.
   */
  @Override
  public short readShort() throws IOException {
    return (short) readUnsignedShort();
  }

  /**
   * Reads a char as specified by {@link DataInputStream#readChar()}, except using little-endian
   * byte order.
   *
   * @return the next two bytes of the input stream, interpreted as a {@code char} in little-endian
   *     byte order
   * @throws IOException if an I/O error occurs
   */
  @Override
  public char readChar() throws IOException {
    return (char) readUnsignedShort();
  }

  @Override
  public byte readByte() throws IOException {
    return (byte) readUnsignedByte();
  }

  @Override
  public boolean readBoolean() throws IOException {
    return readUnsignedByte() != 0;
  }

  /**
   * Reads a byte from the input stream checking that the end of file (EOF) has not been
   * encountered.
   *
   * @return byte read from input
   * @throws IOException if an error is encountered while reading
   * @throws EOFException if the end of file (EOF) is encountered.
   */
  private byte readAndCheckByte() throws IOException, EOFException {
    int b1 = in.read();

    if (-1 == b1) {
      throw new EOFException();
    }

    return (byte) b1;
  }
}
