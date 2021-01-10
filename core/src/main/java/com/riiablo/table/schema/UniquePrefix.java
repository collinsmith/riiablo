package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class UniquePrefix {
  @Override
  public String toString() {
    return Name;
  }

  @PrimaryKey
  public String Name;
}
