package com.riiablo.table.schema;

import com.riiablo.table.Serializer;
import com.riiablo.table.Table;

public abstract class MonStatsTableImpl extends Table<MonStats> {
  public MonStatsTableImpl() {
    super(MonStats.class);
  }

  @Override
  protected MonStats newRecord() {
    return new MonStats();
  }

  @Override
  protected Serializer<MonStats> newSerializer() {
    return new MonStatsSerializerImpl();
  }
}
