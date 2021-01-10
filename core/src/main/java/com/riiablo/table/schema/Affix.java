package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;

@SuppressWarnings("unused")
public class Affix {
  @Override
  public String toString() {
    return name;
  }

  @PrimaryKey
  public String name;

  public int version;
  public int add;
  public int multiply;
  public int divide;
}
