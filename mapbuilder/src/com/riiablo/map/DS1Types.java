package com.riiablo.map;

import com.riiablo.codec.excel.Excel;

public class DS1Types extends Excel<DS1Types.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Column public String Name;

    @Column @Key
    public int    Def;

    @Column public int    LevelType;
  }
}
