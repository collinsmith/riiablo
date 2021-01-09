package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
public class CompCode {
  @Override
  public String toString() {
    return component;
  }

  public String component;

  @PrimaryKey
  public String code;
}
