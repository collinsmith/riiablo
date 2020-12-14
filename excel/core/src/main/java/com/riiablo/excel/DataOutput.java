package com.riiablo.excel;

public interface DataOutput {
  void write8(int value);
  void write16(int value);
  void write32(int value);
  void write64(long value);
  void writeBoolean(boolean value);
  void writeString(CharSequence chars);
}
