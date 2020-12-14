package com.riiablo.excel;

public interface DataInput {
  byte read8();
  short read16();
  int read32();
  long read64();
  boolean readBoolean();
  String readString();
}
