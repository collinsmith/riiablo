package com.riiablo.codec.excel;

@Excel.Binned
public class MonMode extends Excel<MonMode.Entry> {
  public static class Entry extends ModeEntry {
    public String getCode() {
      return Code;
    }

    @Column
    @Key
    public String Code;
  }
}
