package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class ModeEntry extends Excel.Entry {
  public String getCode() {
    return Token;
  }

  @Override
  public String toString() {
    return Name;
  }

  @Column
  public String Name;

  @Column
  @Key
  public String Token;
}
