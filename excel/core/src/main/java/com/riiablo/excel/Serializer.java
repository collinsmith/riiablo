package com.riiablo.excel;

public interface Serializer<T> {
  void readRecord(T record, DataInput in);
  void writeRecord(T record, DataOutput out);
  boolean equals(T e1, T e2);
  Iterable<Throwable> compare(T e1, T e2);
}
