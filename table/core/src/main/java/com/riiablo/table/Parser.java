package com.riiablo.table;

/**
 * Defines behaviors necessary to parse a record to and from a tsv format.
 *
 * @param <R> record type
 */
public interface Parser<R> {
  void parseFields(final ParserInput parser);
  R parseRecord(final int recordId, final ParserInput parser, final R record);
}
