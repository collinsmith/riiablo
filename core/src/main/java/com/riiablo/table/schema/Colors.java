package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Colors {
  @Override
  public String toString() {
    return Transform_Color;
  }

  @Format(format = "Transform Color")
  public String Transform_Color;

  @PrimaryKey
  public String Code;
}
