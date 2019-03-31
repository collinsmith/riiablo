package com.riiablo.codec.excel;

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
    @Column public String  qsts;
    @Column public int     order;
    @Column public boolean visible;
    @Column public String  icon;
    @Column public int     questdone;
  }
}
