package com.riiablo.codec.excel;

@Excel.Binned
public class Misc extends Excel<Misc.Entry> {
  public static class Entry extends ItemEntry {
    @Column public int     pSpell;
    @Column public int     spelldesc;
    @Column public String  spelldescstr;
  }
}
