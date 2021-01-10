package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Composit {
  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public String Token;
}
