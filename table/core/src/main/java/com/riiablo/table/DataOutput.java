package com.riiablo.table;

public interface DataOutput {
  DataOutput write8(int value);
  DataOutput write16(int value);
  DataOutput write32(int value);
  DataOutput write64(long value);
  DataOutput writeBoolean(boolean value);
  DataOutput writeString(CharSequence chars);
}
