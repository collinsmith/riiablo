package com.riiablo.codec.excel;

@Excel.Binned
public class CompCode extends Excel<CompCode.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return component;
    }

    @Column
    public String component;

    @Column
    @Key
    public String code;
  }
}
