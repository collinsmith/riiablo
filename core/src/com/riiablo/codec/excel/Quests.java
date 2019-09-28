package com.riiablo.codec.excel;

@Excel.Binned
public class Quests extends Excel<Quests.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return name;
    }

    @Key
    @Column
    public int     id;
    @Column public String  name;
    @Column public int     act;
    @Column public int     order;
    @Column public boolean visible;
    @Column public String  icon;
    @Column public int     questdone;
    @Column public String  qstr;
    @Column(startIndex = 1, endIndex = 7)
    public String  qsts[];
    @Column(format = "qsts%da", startIndex = 1, endIndex = 7)
    public String  qstsa[];
    @Column(format = "qsts%db", startIndex = 1, endIndex = 7)
    public String  qstsb[];
  }
}
