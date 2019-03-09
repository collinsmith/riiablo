package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class UniquePrefix extends Excel<UniquePrefix.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Name;
    }

    @Key
    @Column
    public String  Name;
  }
}
