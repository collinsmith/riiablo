package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
public class CharStats {
  @Override
  public String toString() {
    return _class;
  }

  @PrimaryKey
  @Format(format = "class")
  public String _class;

  public int str;
  public int dex;

  @Format(format = "int")
  public int _int;

  public int vit;
  public int stamina;
  public int hpadd;
  public int ManaRegen;
  public int ToHitFactor;
  public int WalkVelocity;
  public int RunVelocity;
  public int RunDrain;
  public int LifePerLevel;
  public int StaminaPerLevel;
  public int ManaPerLevel;
  public int LifePerVitality;
  public int StaminaPerVitality;
  public int ManaPerMagic;
  public int StatPerLevel;
  public int BlockFactor;
  public String StrAllSkills;

  @Format(
      startIndex = 1,
      endIndex = 4)
  public String StrSkillTab[];

  public String StrClassOnly;
  public String baseWClass;

  @Format(
      format = "Skill %d",
      startIndex = 1,
      endIndex = 11)
  public String Skill[];

  @Format(
      format = "item%d",
      startIndex = 1,
      endIndex = 11)
  public String item[];

  @Format(
      format = "item%dloc",
      startIndex = 1,
      endIndex = 11)
  public String itemloc[];

  @Format(
      format = "item%dcount",
      startIndex = 1,
      endIndex = 11)
  public String itemcount[];
}
