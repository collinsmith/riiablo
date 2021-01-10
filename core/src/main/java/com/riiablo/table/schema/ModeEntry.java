package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;

@SuppressWarnings("unused")
public class ModeEntry {
  public String getCode() {
    return Token;
  }

  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public String Token;
}
