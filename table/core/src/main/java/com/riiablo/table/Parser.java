package com.riiablo.table;

/**
 * Defines behaviors necessary to parse a record to and from a tsv format.
 *
 * @param <R> record type
 */
public abstract class Parser<R> {
  private final ParserInput parser;
  private boolean fieldsParsed = false;

  protected Parser(ParserInput parser) {
    this.parser = parser;
  }

  public ParserInput parser() {
    return parser;
  }

  protected abstract Parser<R> _parseFields(ParserInput parser);
  protected abstract R _parseRecord(ParserInput parser, int recordId, R record);

  final Parser<R> parseFields() {
    if (fieldsParsed) throw new IllegalStateException("fields have already been parsed!");
    fieldsParsed = true;
    return _parseFields(parser);
  }

  final R parseRecord(int recordId, R record) {
    return _parseRecord(parser, recordId, record);
  }
}
