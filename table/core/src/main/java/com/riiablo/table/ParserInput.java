package com.riiablo.table;

public interface ParserInput {
  int fieldId(String fieldName);
  int numFields();

  int recordId(String recordName);
  int numRecords();

  String recordName(int recordId);
  int primaryKey(String fieldName);
  int primaryKey();

  CharSequence token(int recordId, int fieldId);

  byte parseByte(int recordId, int fieldId);
  short parseShort(int recordId, int fieldId);
  int parseInt(int recordId, int fieldId);
  long parseLong(int recordId, int fieldId);
  boolean parseBoolean(int recordId, int fieldId);
  float parseFloat(int recordId, int fieldId);
  double parseDouble(int recordId, int fieldId);
  String parseString(int recordId, int fieldId);
}
