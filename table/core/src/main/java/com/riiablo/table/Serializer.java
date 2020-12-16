package com.riiablo.table;

/**
 * Defines behaviors necessary to serialize a record to and from a binary
 * format.
 *
 * @param <R> record type
 */
public interface Serializer<R> {
  void readRecord(R record, DataInput in);
  void writeRecord(R record, DataOutput out);
  boolean equals(R e1, R e2);
  Iterable<Throwable> compare(R e1, R e2);
}
