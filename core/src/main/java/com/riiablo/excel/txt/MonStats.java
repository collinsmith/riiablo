package com.riiablo.excel.txt;

import com.riiablo.excel.Excel;
import com.riiablo.io.ByteInput;
import com.riiablo.io.ByteOutput;

public class MonStats extends Excel<MonStats.Entry, MonStats.Serializer> {
  public MonStats() {
    super(Entry.class);
  }

  @Override
  public Entry newEntry() {
    return new Entry();
  }

  @Override
  public Serializer newSerializer() {
    return new Serializer();
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return NameStr;
    }

    @Key
    @Column public String  Id;
    @Column public int     hcIdx;
    @Column public String  BaseId;
    @Column public String  NextInClass;
    @Column public int     TransLvl;
    @Column public String  NameStr;
    @Column public String  MonStatsEx;
    @Column public String  MonProp;
    @Column public String  MonType;
    @Column public String  AI;
    @Column public String  DescStr;
    @Column public String  Code;
    @Column public boolean enabled;
    @Column public boolean rangedtype;
    @Column public boolean placespawn;
    @Column public String  spawn;
    @Column public int     spawnx;
    @Column public int     spawny;
    @Column public String  spawnmode;
    @Column public String  minion1;
    @Column public String  minion2;
    @Column public boolean SetBoss;
    @Column public boolean BossXfer;
    @Column public int     PartyMin;
    @Column public int     PartyMax;
    @Column public int     MinGrp;
    @Column public int     MaxGrp;
    @Column public int     sparsePopulate;
    @Column public int     Velocity;
    @Column public int     Run;
    @Column public int     Rarity;
    @Column(format = "Level%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     Level[];
    @Column public String  MonSound;
    @Column public String  UMonSound;
    @Column public int     threat;
    @Column(format = "aidel%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aidel[];
    @Column(format = "aidist%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aidist[];
    @Column(format = "aip1%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip1[];
    @Column(format = "aip2%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip2[];
    @Column(format = "aip3%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip3[];
    @Column(format = "aip4%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip4[];
    @Column(format = "aip5%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip5[];
    @Column(format = "aip6%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip6[];
    @Column(format = "aip7%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip7[];
    @Column(format = "aip8%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     aip8[];
    @Column public String  MissA1;
    @Column public String  MissA2;
    @Column public String  MissS1;
    @Column public String  MissS2;
    @Column public String  MissS3;
    @Column public String  MissS4;
    @Column public String  MissC;
    @Column public String  MissSQ;
    @Column public int     Align;
    @Column public boolean isSpawn;
    @Column public boolean isMelee;
    @Column public boolean npc;
    @Column public boolean interact;
    @Column public boolean inventory;
    @Column public boolean inTown;
    @Column public boolean lUndead;
    @Column public boolean hUndead;
    @Column public boolean demon;
    @Column public boolean flying;
    @Column public boolean opendoors;
    @Column public boolean boss;
    @Column public boolean primeevil;
    @Column public boolean killable;
    @Column public boolean switchai;
    @Column public boolean noAura;
    @Column public boolean nomultishot;
    @Column public boolean neverCount;
    @Column public boolean petIgnore;
    @Column public boolean deathDmg;
    @Column public boolean genericSpawn;
    @Column public boolean zoo;
    @Column public int     SendSkills;
    @Column public String  Skill1;
    @Column public String  Sk1mode;
    @Column public int     Sk1lvl;
    @Column public String  Skill2;
    @Column public String  Sk2mode;
    @Column public int     Sk2lvl;
    @Column public String  Skill3;
    @Column public String  Sk3mode;
    @Column public int     Sk3lvl;
    @Column public String  Skill4;
    @Column public String  Sk4mode;
    @Column public int     Sk4lvl;
    @Column public String  Skill5;
    @Column public String  Sk5mode;
    @Column public int     Sk5lvl;
    @Column public String  Skill6;
    @Column public String  Sk6mode;
    @Column public int     Sk6lvl;
    @Column public String  Skill7;
    @Column public String  Sk7mode;
    @Column public int     Sk7lvl;
    @Column public String  Skill8;
    @Column public String  Sk8mode;
    @Column public int     Sk8lvl;
    @Column(format = "Drain%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     Drain[];
    @Column(format = "coldeffect%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     coldeffect[];
    @Column(format = "ResDm%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResDm[];
    @Column(format = "ResMa%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResMa[];
    @Column(format = "ResFi%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResFi[];
    @Column(format = "ResLi%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResLi[];
    @Column(format = "ResCo%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResCo[];
    @Column(format = "ResPo%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ResPo[];
    @Column public int     DamageRegen;
    @Column public String  SkillDamage;
    @Column public boolean noRatio;
    @Column public boolean NoShldBlock;
    @Column(format = "ToBlock%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     ToBlock[];
    @Column public int     Crit;
    @Column(format = "minHP%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     minHP[];
    @Column(format = "maxHP%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     maxHP[];
    @Column(format = "AC%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     AC[];
    @Column(format = "Exp%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     Exp[];
    @Column(format = "A1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A1MinD[];
    @Column(format = "A1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A1MaxD[];
    @Column(format = "A1TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A1TH[];
    @Column(format = "A2MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A2MinD[];
    @Column(format = "A2MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A2MaxD[];
    @Column(format = "A2TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     A2TH[];
    @Column(format = "S1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     S1MinD[];
    @Column(format = "S1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     S1MaxD[];
    @Column(format = "S1TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     S1TH[];
    @Column public String  El1Mode;
    @Column public String  El1Type;
    @Column(format = "El1Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El1Pct[];
    @Column(format = "El1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El1MinD[];
    @Column(format = "El1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El1MaxD[];
    @Column(format = "El1Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El1Dur[];
    @Column public String  El2Mode;
    @Column public String  El2Type;
    @Column(format = "El2Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El2Pct[];
    @Column(format = "El2MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El2MinD[];
    @Column(format = "El2MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El2MaxD[];
    @Column(format = "El2Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El2Dur[];
    @Column public String  El3Mode;
    @Column public String  El3Type;
    @Column(format = "El3Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El3Pct[];
    @Column(format = "El3MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El3MinD[];
    @Column(format = "El3MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El3MaxD[];
    @Column(format = "El3Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public int     El3Dur[];
    @Column(format = "TreasureClass1%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public String  TreasureClass1[];
    @Column(format = "TreasureClass2%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public String  TreasureClass2[];
    @Column(format = "TreasureClass3%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public String  TreasureClass3[];
    @Column(format = "TreasureClass4%s", values = {"", "(N)", "(H)"}, endIndex = 3)
    public String  TreasureClass4[];
    @Column public int     TCQuestId;
    @Column public int     TCQuestCP;
    @Column public int     SplEndDeath;
    @Column public boolean SplGetModeChart;
    @Column public boolean SplEndGeneric;
    @Column public boolean SplClientEnd;
  }

  public static class Serializer implements com.riiablo.excel.Serializer<Entry> {
    @Override public void readBin(Entry entry, ByteInput in) {}
    @Override public void writeBin(Entry entry, ByteOutput out) {}
  }
}
