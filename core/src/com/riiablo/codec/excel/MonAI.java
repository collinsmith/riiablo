package com.riiablo.codec.excel;

public class MonAI extends Excel<MonAI.Entry> {
  public static class Entry extends Excel.Entry {
    public String getCode() {
      return AI;
    }

    @Key
    @Column
    public String  AI;
    @Column(format = "*aip%d", startIndex = 1, endIndex = 9)
    public String  aip[];
  }
}
