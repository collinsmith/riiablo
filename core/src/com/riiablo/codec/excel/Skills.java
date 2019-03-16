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
  }
}
