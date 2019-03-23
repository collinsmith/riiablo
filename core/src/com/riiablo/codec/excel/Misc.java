package com.riiablo.codec.excel;

public class Misc extends Excel<Misc.Entry> {
  public static class Entry extends ItemEntry {
    @Column public int     spelldesc;
    @Column public String  spelldescstr;
  }
}
