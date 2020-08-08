package com.riiablo.io.nio;

interface AlignedReader extends Aligned {
  int bytesRead();
  int bytesRemaining();
  int numBytes();

  byte read8();
  short read16();
  int read32();
  long read64();

  short read8u();
  int read16u();
  long read32u();

  byte[] readBytes(int len);
  byte[] readBytes(byte[] dst);
  byte[] readBytes(byte[] dst, int dstOffset, int len);

  String readString(int len);
}
