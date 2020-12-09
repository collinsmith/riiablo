package com.riiablo.excel2;

import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;

public interface Serializer<T extends Excel.Entry> {
  void readBin(T entry, ByteInput in);
  void writeBin(T entry, ByteOutput out);
}
