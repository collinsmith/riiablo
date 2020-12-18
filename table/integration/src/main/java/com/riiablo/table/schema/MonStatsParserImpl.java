package com.riiablo.table.schema;

import com.riiablo.table.Parser;
import com.riiablo.table.ParserInput;

public class MonStatsParserImpl extends Parser<MonStats> {
  int[] fieldIds = new int[5];

  public MonStatsParserImpl(ParserInput parser) {
    super(parser);
  }

  @Override
  protected Parser<MonStats> _parseFields(ParserInput parser) {
    fieldIds[0] = parser.fieldId("A1MaxD1");
    fieldIds[1] = parser.fieldId("A1MaxD2");
    return this;
  }

  // TODO: performance improvement of sorting calls by fieldId
  //       create Function[numFields]: (record) -> record.<field> = parser.parse<type>(fieldId)
  @Override
  protected MonStats _parseRecord(ParserInput parser, int recordId, MonStats record) {
    record.A1MaxD[0] = parser.parseInt(recordId, fieldIds[0]);
    record.A1MaxD[1] = parser.parseInt(recordId, fieldIds[1]);
    return record;
  }
}
