package com.riiablo.codec.excel;

public class Skills extends Excel<Skills.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return skill;
    }

    @Key
    @Column
    public int     Id;
    @Column public String  skill;
    @Column public String  charclass;
    @Column public String  skilldesc;
    @Column public String  stsound;
    @Column public String  castoverlay;
    @Column public String  anim;
    @Column public String  seqtrans;
    @Column public String  monanim;
    @Column public int     seqnum;
    @Column public int     seqinput;
    @Column public int     reqlevel;
    @Column public int     startmana;
    @Column public int     minmana;
    @Column public int     manashift;
    @Column public int     mana;
    @Column public int     lvlmana;
    @Column(startIndex = 1, endIndex = 9)
    public int     Param[];
  }
}
