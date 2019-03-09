package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class Levels extends Excel<Levels.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column public String  Name;
    @Column
    @Key
    public int     Id;
    @Column public int     Pal;
    @Column public int     Act;
    @Column public int     QuestFlag;
    @Column public int     QuestFlagEx;
    @Column public int     Layer;
    @Column(format = "SizeX%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     SizeX[];
    @Column(format = "SizeY%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     SizeY[];
    @Column public int     OffsetX;
    @Column public int     OffsetY;
    @Column public int     Depend;
    @Column public int     Teleport;
    @Column public boolean Rain;
    @Column public boolean Mud;
    @Column public boolean NoPer;
    @Column public boolean LOSDraw;
    @Column public boolean FloorFilter;
    @Column public boolean BlankScreen;
    @Column public boolean DrawEdges;
    @Column public boolean IsInside;
    @Column public int     DrlgType;
    @Column public int     LevelType;
    @Column public int     SubType;
    @Column public int     SubTheme;
    @Column public int     SubWaypoint;
    @Column public int     SubShrine;
    @Column(endIndex = 8)
    public int     Vis[];
    @Column(endIndex = 8)
    public int     Warp[];
    @Column public int     Intensity;
    @Column public int     Red;
    @Column public int     Green;
    @Column public int     Blue;
    @Column public boolean Portal;
    @Column public boolean Position;
    @Column public boolean SaveMonsters;
    @Column public int     Quest;
    @Column public int     WarpDist;
    @Column(startIndex = 1, endIndex = 4)
    public int     MonLvl[];
    @Column(format = "MonLvl%dEx", startIndex = 1, endIndex = 4)
    public int     MonLvlEx[];
    @Column(format = "MonDen%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     MonDen[];
    @Column(format = "MonUMin%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     MonUMin[];
    @Column(format = "MonUMin%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     MonUMax[];
    @Column public boolean MonWndr;
    @Column public int     MonSpcWalk;
    @Column public int     NumMon;
    @Column(startIndex = 1, endIndex = 11)
    public String  mon[];
    @Column public boolean rangedspawn;
    @Column(startIndex = 1, endIndex = 11)
    public String  nmon[];
    @Column(startIndex = 1, endIndex = 11)
    public String  umon[];
    @Column(startIndex = 1, endIndex = 5)
    public String  cmon[];
    @Column(startIndex = 1, endIndex = 5)
    public int     cpct[];
    @Column(startIndex = 1, endIndex = 5)
    public int     camt[];
    @Column public int     Themes;
    @Column public int     SoundEnv;
    @Column public int     Waypoint;
    @Column public String  LevelName;
    @Column public String  LevelWarp;
    @Column public int     EntryFile;
    @Column(endIndex = 8)
    public int     ObjGrp[];
    @Column(endIndex = 8)
    public int     ObjPrb[];
    @Column public boolean Beta;
  }
}
