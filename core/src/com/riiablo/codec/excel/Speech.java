package com.riiablo.codec.excel;

@Excel.Binned
public class Speech extends Excel<Speech.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return sound;
    }

    @Column
    @Key
    public String  sound;
    @Column public String  soundstr;
  }
}
