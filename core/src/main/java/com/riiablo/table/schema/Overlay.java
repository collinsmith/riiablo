package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Overlay {
  @Override
  public String toString() {
    return overlay;
  }

  @PrimaryKey
  public String overlay;

  public String Filename;
  public int version;
  public int Frames;
  public String Character;
  public boolean PreDraw;

  @Format(format = "1ofN")
  public int _1ofN;

  public int Dir;
  public boolean Open;
  public boolean Beta;
  public int Xoffset;
  public int Yoffset;
  public int Height1;
  public int Height2;
  public int Height3;
  public int Height4;
  public int AnimRate;
  public int LoopWaitTime;
  public int Trans;
  public int InitRadius;
  public int Radius;
  public int Red;
  public int Green;
  public int Blue;
  public int NumDirections;
  public boolean LocalBlood;
}
