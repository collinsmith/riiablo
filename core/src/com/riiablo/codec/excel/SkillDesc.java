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
    @Column public String  descdam;
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
    @Column public String  descatt;
    @Column public String  descmissile1;
    @Column public String  descmissile2;
    @Column public String  descmissile3;
    @Column public String  descline1;
    @Column public String  desctexta1;
    @Column public String  desctextb1;
    @Column public String  desccalca1;
    @Column public String  desccalcb1;
    @Column public String  descline2;
    @Column public String  desctexta2;
    @Column public String  desctextb2;
    @Column public String  desccalca2;
    @Column public String  desccalcb2;
    @Column public String  descline3;
    @Column public String  desctexta3;
    @Column public String  desctextb3;
    @Column public String  desccalca3;
    @Column public String  desccalcb3;
    @Column public String  descline4;
    @Column public String  desctexta4;
    @Column public String  desctextb4;
    @Column public String  desccalca4;
    @Column public String  desccalcb4;
    @Column public String  descline5;
    @Column public String  desctexta5;
    @Column public String  desctextb5;
    @Column public String  desccalca5;
    @Column public String  desccalcb5;
    @Column public String  descline6;
    @Column public String  desctexta6;
    @Column public String  desctextb6;
    @Column public String  desccalca6;
    @Column public String  desccalcb6;
    @Column public String  dsc2line1;
    @Column public String  dsc2texta1;
    @Column public String  dsc2textb1;
    @Column public String  dsc2calca1;
    @Column public String  dsc2calcb1;
    @Column public String  dsc2line2;
    @Column public String  dsc2texta2;
    @Column public String  dsc2textb2;
    @Column public String  dsc2calca2;
    @Column public String  dsc2calcb2;
    @Column public String  dsc2line3;
    @Column public String  dsc2texta3;
    @Column public String  dsc2textb3;
    @Column public String  dsc2calca3;
    @Column public String  dsc2calcb3;
    @Column public String  dsc2line4;
    @Column public String  dsc2texta4;
    @Column public String  dsc2textb4;
    @Column public String  dsc2calca4;
    @Column public String  dsc2calcb4;
    @Column public String  dsc3line1;
    @Column public String  dsc3texta1;
    @Column public String  dsc3textb1;
    @Column public String  dsc3calca1;
    @Column public String  dsc3calcb1;
    @Column public String  dsc3line2;
    @Column public String  dsc3texta2;
    @Column public String  dsc3textb2;
    @Column public String  dsc3calca2;
    @Column public String  dsc3calcb2;
    @Column public String  dsc3line3;
    @Column public String  dsc3texta3;
    @Column public String  dsc3textb3;
    @Column public String  dsc3calca3;
    @Column public String  dsc3calcb3;
    @Column public String  dsc3line4;
    @Column public String  dsc3texta4;
    @Column public String  dsc3textb4;
    @Column public String  dsc3calca4;
    @Column public String  dsc3calcb4;
    @Column public String  dsc3line5;
    @Column public String  dsc3texta5;
    @Column public String  dsc3textb5;
    @Column public String  dsc3calca5;
    @Column public String  dsc3calcb5;
    @Column public String  dsc3line6;
    @Column public String  dsc3texta6;
    @Column public String  dsc3textb6;
    @Column public String  dsc3calca6;
    @Column public String  dsc3calcb6;
    @Column public String  dsc3line7;
    @Column public String  dsc3texta7;
    @Column public String  dsc3textb7;
    @Column public String  dsc3calca7;
    @Column public String  dsc3calcb7;
  }
}
