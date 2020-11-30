package com.riiablo.excel;

import java.io.IOException;

import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;

public interface Serializer<T extends Excel.Entry> {
  void readBin(T entry, ByteInput in) throws IOException;
  void writeBin(T entry, ByteOutput out) throws IOException;
}
