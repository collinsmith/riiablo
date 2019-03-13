package com.riiablo.codec.excel;

public class LvlWarp extends Excel<LvlWarp.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column public String  Name;
    @Column
    @Key
    public int     Id;
    @Column public int     SelectX;
    @Column public int     SelectY;
    @Column public int     SelectDX;
    @Column public int     SelectDY;
    @Column public int     ExitWalkX;
    @Column public int     ExitWalkY;
    @Column public int     OffsetX;
    @Column public int     OffsetY;
    @Column public boolean LitVersion;
    @Column public int     Tiles;
    @Column public String  Direction;
    @Column public boolean Beta;
  }
}
