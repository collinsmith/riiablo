package com.riiablo.table.schema;

import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class LvlWarp {
  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public int Id;

  public int SelectX;
  public int SelectY;
  public int SelectDX;
  public int SelectDY;
  public int ExitWalkX;
  public int ExitWalkY;
  public int OffsetX;
  public int OffsetY;
  public boolean LitVersion;
  public int Tiles;
  public String Direction;
  public boolean Beta;
}
