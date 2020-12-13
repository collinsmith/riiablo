package com.riiablo.excel;

import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;

public interface Serializer<T extends Excel.Entry> {
  void readBin(T entry, ByteInput in);
  void writeBin(T entry, ByteOutput out);
  boolean equals(T e1, T e2);
  void logErrors(T e1, T e2);
}
