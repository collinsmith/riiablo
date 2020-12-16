package com.riiablo.table.schema;

import com.riiablo.table.DataInput;
import com.riiablo.table.DataOutput;
import com.riiablo.table.Serializer;

public class MonStatsSerializerImpl implements Serializer<MonStats> {
  @Override
  public void readRecord(MonStats record, DataInput in) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeRecord(MonStats record, DataOutput out) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(MonStats e1, MonStats e2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<Throwable> compare(MonStats e1, MonStats e2) {
    throw new UnsupportedOperationException();
  }

}
