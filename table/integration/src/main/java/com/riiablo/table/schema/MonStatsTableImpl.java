package com.riiablo.table.schema;

import com.riiablo.table.Parser;
import com.riiablo.table.ParserInput;
import com.riiablo.table.Serializer;
import com.riiablo.table.Table;

public class MonStatsTableImpl extends Table<MonStats> {
  public MonStatsTableImpl() {
    super(MonStats.class);
  }

  @Override
  protected MonStats newRecord() {
    return new MonStats();
  }

  @Override
  protected Parser<MonStats> newParser(ParserInput parser) {
    return new MonStatsParserImpl(parser);
  }

  @Override
  protected Serializer<MonStats> newSerializer() {
    return new MonStatsSerializerImpl();
  }
}
