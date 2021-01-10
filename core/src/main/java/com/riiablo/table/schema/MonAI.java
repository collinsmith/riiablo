package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class MonAI {
  public String getCode() {
    return AI;
  }

  @PrimaryKey
  public String  AI;

  @Format(
      format = "*aip%d",
      startIndex = 1,
      endIndex = 9)
  public String  aip[];
}
