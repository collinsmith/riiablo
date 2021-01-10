package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Quests {
  @Override
  public String toString() {
    return name;
  }

  @PrimaryKey
  public int id;

  public String name;
  public int act;
  public int order;
  public boolean visible;
  public String icon;
  public int questdone;
  public String qstr;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String qsts[];

  @Format(
      format = "qsts%da",
      startIndex = 1,
      endIndex = 7)
  public String qstsa[];

  @Format(
      format = "qsts%db",
      startIndex = 1,
      endIndex = 7)
  public String qstsb[];
}
