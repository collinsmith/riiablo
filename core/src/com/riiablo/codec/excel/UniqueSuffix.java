package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class UniqueSuffix extends Excel<UniqueSuffix.Entry> {
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
