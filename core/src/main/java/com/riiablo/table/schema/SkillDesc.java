package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class SkillDesc {
  @Override
  public String toString() {
    return skilldesc;
  }

  @PrimaryKey
  public String skilldesc;

  public int SkillPage;
  public int SkillRow;
  public int SkillColumn;
  public int ListRow;
  public int ListPool;
  public int IconCel;

  @Format(format = "str name")
  public String str_name;

  @Format(format = "str short")
  public String str_short;

  @Format(format = "str long")
  public String str_long;

  @Format(format = "str alt")
  public String str_alt;

  @Format(format = "str mana")
  public String str_mana;

  public int descdam;

  @Format(format = "ddam calc1")
  public String ddam_calc1;

  @Format(format = "ddam calc2")
  public String ddam_calc2;

  public String p1dmelem;
  public String p1dmmin;
  public String p1dmmax;
  public String p2dmelem;
  public String p2dmmin;
  public String p2dmmax;
  public String p3dmelem;
  public String p3dmmin;
  public String p3dmmax;
  public int descatt;
  public String descmissile1;
  public String descmissile2;
  public String descmissile3;

  @Format(
      startIndex = 1,
      endIndex = 7)
  public int descline[];

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String desctexta[];

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String desctextb[];

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String desccalca[];

  @Format(
      startIndex = 1,
      endIndex = 7)
  public String desccalcb[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public int dsc2line[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String dsc2texta[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String dsc2textb[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String dsc2calca[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String dsc2calcb[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public int dsc3line[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String dsc3texta[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String dsc3textb[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String dsc3calca[];

  @Format(
      startIndex = 1,
      endIndex = 8)
  public String dsc3calcb[];
}
