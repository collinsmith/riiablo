package com.riiablo.table;

/**
 * @deprecated unused -- attempt to create automatic mapping of txt parser records to indexes
 */
@Deprecated
public interface ParserMapper<P extends Parser<?>> {
  int map(P parser, String recordName);
}
