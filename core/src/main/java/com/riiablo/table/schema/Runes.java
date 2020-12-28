package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.RunesTable;

@Schema(
    offset = 26,
    preload = true
)
@Table(RunesTable.class)
@SuppressWarnings("unused")
public class Runes {
  private static final String TAG = "Runes";
  private static final boolean DEBUG = !true;

  @Override
  public String toString() {
    return Rune_Name;
  }

  @PrimaryKey
  public String Name;

  @Format(format = "Rune Name")
  public String Rune_Name;

  public boolean complete;

  public boolean server;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String itype[];

  @Format(
      startIndex = 1,
      endIndex = 4)
  public String etype[];

  @Format(format = "*runes")
  public String _runes;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String Rune[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String T1Code[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String T1Param[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String T1Min[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String T1Max[];
}
