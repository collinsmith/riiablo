package com.riiablo.io.nio;

interface UnalignedReader extends Aligned, AlignedReader {
  int MAX_ULONG_BITS = Long.SIZE - 1;
  int MAX_UINT_BITS = Integer.SIZE - 1;
  int MAX_USHORT_BITS = Short.SIZE - 1;
  int MAX_UBYTE_BITS = Byte.SIZE - 1;
  int MAX_UNSIGNED_BITS = MAX_ULONG_BITS;

  int bitsCached();
  long cache();

  long bitsRead();
  long bitsRemaining();
  long numBits();

  /**
   * Reads up to {@value #MAX_UBYTE_BITS} bits as unsigned and casts the result
   * into a {@code byte}.
   */
  byte read7u(int bits);

  /**
   * Reads up to {@value #MAX_USHORT_BITS} bits as unsigned and casts the
   * result into a {@code short}.
   */
  short read15u(int bits);

  /**
   * Reads up to {@value #MAX_UINT_BITS} bits as unsigned and casts the result
   * into a {@code int}.
   */
  int read31u(int bits);

  /**
   * Reads up to {@value #MAX_ULONG_BITS} bits as unsigned and casts the result
   * into a {@code long}.
   */
  long read63u(int bits);

  /**
   * Reads {@code 1} bit as a {@code boolean}.
   *
   * @see #read1()
   */
  boolean readBoolean();

  /**
   * Reads {@code 1} bit as a {@code byte}.
   */
  byte read1();

  /**
   * Reads up to {@value Byte#SIZE} bits as a sign-extended {@code byte}.
   */
  byte read8(int bits);

  /**
   * Reads up to {@value Short#SIZE} bits as a sign-extended {@code short}.
   */
  short read16(int bits);

  /**
   * Reads up to {@value Integer#SIZE} bits as a sign-extended {@code int}.
   */
  int read32(int bits);

  /**
   * Reads up to {@value Long#SIZE} bits as a sign-extended {@code long}.
   */
  long read64(int bits);

  /**
   * Reads up to {@value Long#SIZE} bits as a {@code long}. This method is
   * intended to be used to read raw memory (i.e., flags).
   */
  long readRaw(int bits);

  /**
   * Reads <i>n</i> characters of size {@code bits} and constructs a string.
   *
   * @param len number of characters to read
   * @param bits size of each character ({@code 7} or {@code 8})
   * @param nullTerminated {@code true} to stop reading at {@code \0}, otherwise
   *     {@code len} characters will be read (variable-width string)
   */
  String readString(int len, int bits, boolean nullTerminated);
}
