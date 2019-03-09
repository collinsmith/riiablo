package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;
import com.riiablo.codec.excel.ModeEntry;

public class PlrMode extends Excel<PlrMode.Entry> {
  public static class Entry extends ModeEntry {
    public String getCode() {
      return Code;
    }

    @Column
    @Key
    public String Code;
  }
}
