package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class PlayerClass {
  @Override
  public String toString() {
    return PlayerClass;
  }

  @Format(format = "Player Class")
  public String PlayerClass;

  @PrimaryKey
  public String Code;
}
