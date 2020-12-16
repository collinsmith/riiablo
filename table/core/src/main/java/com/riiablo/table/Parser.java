package com.riiablo.table;

import java.io.IOException;

/**
 * Defines behaviors necessary to parse a record to and from a tsv format.
 *
 * @param <R> record type
 */
public interface Parser<R> {
  void parseFields(final TsvParser parser);
  boolean hasNext(final TsvParser parser) throws IOException;
  void parseRecord(final R record, final TsvParser parser);
}
