package com.riiablo.table.schema;

import java.io.IOException;
import javax.annotation.Generated;

import com.riiablo.table.Parser;
import com.riiablo.table.TsvParser;

public abstract class MonStatsParserImpl implements Parser<MonStats> {
  @Override
  public boolean hasNext(TsvParser parser) throws IOException {
    return parser.cacheLine() != -1;
  }

  // TODO: performance improvement of sorting calls by fieldId
  //       create Function[numFields]: (record) -> record.<field> = parser.parse<type>(fieldId)
  @Generated(value = "")
  public void parseRecord(final MonStats record, final TsvParser parser) {
    record.A1MaxD[0] = parser.parseInt(0);
    record.A1MaxD[1] = parser.parseInt(5);
  }
}
