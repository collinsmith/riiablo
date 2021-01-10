package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class MonMode extends ModeEntry {
  @Override
  public String getCode() {
    return Code;
  }

  @PrimaryKey
  public String Code;
}
