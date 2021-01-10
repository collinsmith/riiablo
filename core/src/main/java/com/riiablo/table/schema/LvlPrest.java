package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class LvlPrest {
  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public int Def;

  public int LevelId;
  public boolean Populate;
  public boolean Logicals;
  public boolean Outdoors;
  public boolean Animate;
  public boolean KillEdge;
  public boolean FillBlanks;
  public int SizeX;
  public int SizeY;
  public boolean AutoMap;
  public boolean Scan;
  public int Pops;
  public int PopPad;
  public int Files;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String File[];

  public int Dt1Mask;
  public boolean Beta;
  public boolean Expansion;
}
