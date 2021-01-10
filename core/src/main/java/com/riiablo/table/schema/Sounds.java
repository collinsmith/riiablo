package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;
import com.riiablo.table.table.SoundsTable;

@Schema
@Table(SoundsTable.class)
@SuppressWarnings("unused")
public class Sounds {
  @Override
  public String toString() {
    return Sound;
  }

  @PrimaryKey
  public String Sound;

  public int Index;
  public String FileName;
  public int Volume;

  @Format(format = "Group Size")
  public int Group_Size;

  public boolean Loop;

  @Format(format = "Fade In")
  public int Fade_In;

  @Format(format = "Fade Out")
  public int Fade_Out;

  @Format(format = "Defer Inst")
  public boolean Defer_Inst;

  @Format(format = "Stop Inst")
  public boolean Stop_Inst;

  public int Duration;
  public int Compound;
  public int Reverb; // Marked boolean, but some rows are int
  public int Falloff;
  public boolean Cache;

  @Format(format = "Async Only")
  public boolean Async_Only;

  public int Priority;
  public boolean Stream;
  public boolean Stereo;
  public boolean Tracking;
  public boolean Solo;

  @Format(format = "Music Vol")
  public boolean Music_Vol;

  @Format(format = "Block 1")
  public String Block_1;

  @Format(format = "Block 2")
  public String Block_2;

  @Format(format = "Block 3")
  public String Block_3;
}
