package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Levels {
  @Override
  public String toString() {
    return Name;
  }

  public String Name;

  @PrimaryKey
  public int Id;

  public int Pal;
  public int Act;
  public int QuestFlag;
  public int QuestFlagEx;
  public int Layer;
  @Format(
      format = "SizeX%s",
      values = {"", "(N)", "(H)"},
      endIndex = 3)
  public int SizeX[];

  @Format(
      format = "SizeY%s",
      values = {"", "(N)", "(H)"},
      endIndex = 3)
  public int SizeY[];

  public int OffsetX;
  public int OffsetY;
  public int Depend;
  public int Teleport;
  public boolean Rain;
  public boolean Mud;
  public boolean NoPer;
  public boolean LOSDraw;
  public boolean FloorFilter;
  public boolean BlankScreen;
  public boolean DrawEdges;
  public boolean IsInside;
  public int DrlgType;
  public int LevelType;
  public int SubType;
  public int SubTheme;
  public int SubWaypoint;
  public int SubShrine;

  @Format(endIndex = 8)
  public int Vis[];

  @Format(endIndex = 8)
  public int Warp[];

  public int Intensity;
  public int Red;
  public int Green;
  public int Blue;
  public boolean Portal;
  public boolean Position;
  public boolean SaveMonsters;
  public int Quest;
  public int WarpDist;
  @Format(
      startIndex = 1,
      endIndex = 4)
  public int MonLvl[];

  @Format(
      format = "MonLvl%dEx",
      startIndex = 1,
      endIndex = 4)
  public int MonLvlEx[];

  @Format(
      format = "MonDen%s",
      values = {"", "(N)", "(H)"},
      endIndex = 3)
  public int MonDen[];

  @Format(
      format = "MonUMin%s",
      values = {"", "(N)", "(H)"},
      endIndex = 3)
  public int MonUMin[];

  @Format(
      format = "MonUMin%s",
      values = {"", "(N)", "(H)"},
      endIndex = 3)
  public int MonUMax[];

  public boolean MonWndr;
  public int MonSpcWalk;
  public int NumMon;

  @Format(
      startIndex = 1,
      endIndex = 11)
  public String mon[];

  public boolean rangedspawn;

  @Format(
      startIndex = 1,
      endIndex = 11)
  public String nmon[];

  @Format(
      startIndex = 1,
      endIndex = 11)
  public String umon[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String cmon[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public int cpct[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public int camt[];

  public int Themes;
  public int SoundEnv;
  public int Waypoint;
  public String LevelName;
  public String LevelWarp;
  public String EntryFile;

  @Format(endIndex = 8)
  public int ObjGrp[];

  @Format(endIndex = 8)
  public int ObjPrb[];

  public boolean Beta;
}
