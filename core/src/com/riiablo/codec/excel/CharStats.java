package com.riiablo.codec.excel;

public class CharStats extends Excel<CharStats.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return _class;
    }

    @Key
    @Column(format = "class")
    public String  _class;
    @Column public int     str;
    @Column public int     dex;
    @Column(format = "int")
    public int     _int;
    @Column public int     vit;
    @Column public int     WalkVelocity;
    @Column public int     RunVelocity;
    @Column public String  StrAllSkills;
    @Column(startIndex = 1, endIndex = 4)
    public String  StrSkillTab[];
    @Column public String  StrClassOnly;
  }
}
