package com.riiablo.codec.excel;

@Excel.Binned
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
    @Column public int     stamina;
    @Column public int     hpadd;
    @Column public int     ManaRegen;
    @Column public int     ToHitFactor;
    @Column public int     WalkVelocity;
    @Column public int     RunVelocity;
    @Column public int     RunDrain;
    @Column public int     LifePerLevel;
    @Column public int     StaminaPerLevel;
    @Column public int     ManaPerLevel;
    @Column public int     LifePerVitality;
    @Column public int     StaminaPerVitality;
    @Column public int     ManaPerMagic;
    @Column public int     StatPerLevel;
    @Column public int     BlockFactor;
    @Column public String  StrAllSkills;
    @Column(startIndex = 1, endIndex = 4)
    public String  StrSkillTab[];
    @Column public String  StrClassOnly;
    @Column public String  baseWClass;
    @Column(format = "Skill %d", startIndex = 1, endIndex = 11)
    public String  Skill[];
    @Column(format = "item%d", startIndex = 1, endIndex = 11)
    public String  item[];
    @Column(format = "item%dloc", startIndex = 1, endIndex = 11)
    public String  itemloc[];
    @Column(format = "item%dcount", startIndex = 1, endIndex = 11)
    public String  itemcount[];
  }
}
