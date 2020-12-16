package com.riiablo.table;

import java.io.IOException;

public interface TsvParser {
  int cacheLine() throws IOException;
  int fieldId(String fieldName);
  byte parseByte(int fieldId);
  short parseShort(int fieldId);
  int parseInt(int fieldId);
  long parseLong(int fieldId);
  boolean parseBoolean(int fieldId);
  float parseFloat(int fieldId);
  double parseDouble(int fieldId);
  String parseString(int fieldId);
}
