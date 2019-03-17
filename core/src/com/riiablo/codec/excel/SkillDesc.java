package com.riiablo.codec.excel;

public class SkillDesc extends Excel<SkillDesc.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return skilldesc;
    }

    @Key
    @Column
    public String  skilldesc;
    @Column public int     SkillPage;
    @Column public int     SkillRow;
    @Column public int     SkillColumn;
    @Column public int     ListRow;
    @Column public int     ListPool;
    @Column public int     IconCel;
    @Column(format = "str name")
    public String  str_name;
    @Column(format = "str short")
    public String  str_short;
    @Column(format = "str long")
    public String  str_long;
    @Column(format = "str alt")
    public String  str_alt;
    @Column(format = "str mana")
    public String  str_mana;
    @Column public int     descdam;
    @Column(format = "ddam calc1")
    public String  ddam_calc1;
    @Column(format = "ddam calc2")
    public String  ddam_calc2;
    @Column public String  p1dmelem;
    @Column public String  p1dmmin;
    @Column public String  p1dmmax;
    @Column public String  p2dmelem;
    @Column public String  p2dmmin;
    @Column public String  p2dmmax;
    @Column public String  p3dmelem;
    @Column public String  p3dmmin;
    @Column public String  p3dmmax;
    @Column public int     descatt;
    @Column public String  descmissile1;
    @Column public String  descmissile2;
    @Column public String  descmissile3;
    @Column(startIndex = 1, endIndex = 7)
    public int     descline[];
    @Column(startIndex = 1, endIndex = 7)
    public String  desctexta[];
    @Column(startIndex = 1, endIndex = 7)
    public String  desctextb[];
    @Column(startIndex = 1, endIndex = 7)
    public String  desccalca[];
    @Column(startIndex = 1, endIndex = 7)
    public String  desccalcb[];
    @Column(startIndex = 1, endIndex = 5)
    public int     dsc2line[];
    @Column(startIndex = 1, endIndex = 5)
    public String  dsc2texta[];
    @Column(startIndex = 1, endIndex = 5)
    public String  dsc2textb[];
    @Column(startIndex = 1, endIndex = 5)
    public String  dsc2calca[];
    @Column(startIndex = 1, endIndex = 5)
    public String  dsc2calcb[];
    @Column(startIndex = 1, endIndex = 8)
    public int     dsc3line[];
    @Column(startIndex = 1, endIndex = 8)
    public String  dsc3texta[];
    @Column(startIndex = 1, endIndex = 8)
    public String  dsc3textb[];
    @Column(startIndex = 1, endIndex = 8)
    public String  dsc3calca[];
    @Column(startIndex = 1, endIndex = 8)
    public String  dsc3calcb[];
  }
}
