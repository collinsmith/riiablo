package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema(indexed = true)
@SuppressWarnings("unused")
public class LowQualityItems {
  @Override
  public String toString() {
    return Name;
  }

  @PrimaryKey
  public String Name;
}
