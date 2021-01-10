package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class ArmType {
  @Override
  public String toString() {
    return Name;
  }

  @PrimaryKey
  public String Token;
  public String Name;
}
