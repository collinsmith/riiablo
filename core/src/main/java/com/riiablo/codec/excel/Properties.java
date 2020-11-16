package com.riiablo.codec.excel;

@Excel.Binned
public class Properties extends Excel<Properties.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return code;
    }

    @Key
    @Column
    public String  code;
    @Column(startIndex = 1, endIndex = 8)
    public int     set[];
    @Column(startIndex = 1, endIndex = 8)
    public int     val[];
    @Column(startIndex = 1, endIndex = 8)
    public int     func[];
    @Column(startIndex = 1, endIndex = 8)
    public String  stat[];
  }
}
