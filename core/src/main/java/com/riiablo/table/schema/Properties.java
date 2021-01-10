package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Properties {
  @Override
  public String toString() {
    return code;
  }

  @PrimaryKey
  public String code;

  @Format(
      startIndex = 1,
      endIndex = 8)
  public int set[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public int val[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public int func[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String stat[];
}
