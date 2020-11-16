package com.riiablo.codec.excel;

@Excel.Binned
public class Composit extends Excel<Composit.Entry> {
  public static class Entry extends Excel.Entry {
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
}
