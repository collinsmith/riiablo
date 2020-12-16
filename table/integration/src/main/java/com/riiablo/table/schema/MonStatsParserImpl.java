package com.riiablo.table.schema;

import java.io.IOException;

import com.riiablo.table.Parser;
import com.riiablo.table.TsvParser;

public class MonStatsParserImpl implements Parser<MonStats> {
  int[] fieldIds = new int[5];

  @Override
  public boolean hasNext(TsvParser parser) throws IOException {
    return parser.cacheLine() != -1;
  }

  @Override
  public void parseFields(TsvParser parser) {
    fieldIds[0] = parser.fieldId("A1MaxD1");
    fieldIds[1] = parser.fieldId("A1MaxD2");
  }

  // TODO: performance improvement of sorting calls by fieldId
  //       create Function[numFields]: (record) -> record.<field> = parser.parse<type>(fieldId)
  @Override
  public void parseRecord(final MonStats record, final TsvParser parser) {
    record.A1MaxD[0] = parser.parseInt(fieldIds[0]);
    record.A1MaxD[1] = parser.parseInt(fieldIds[1]);
  }
}
