package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;
import com.riiablo.table.annotation.Table;

@Schema
@Table(MonStatsTableImpl.class)
// @Serializer(MonStatsSerializerImpl.class)
@SuppressWarnings("unused")
public class MonStats {
  @Override
  public String toString() {
    return NameStr;
  }

  @PrimaryKey
  public String Id;
  public int hcIdx;
  public String BaseId;
  public String NextInClass;
  public int TransLvl;
  public String NameStr;
  public String MonStatsEx;
  public String MonProp;
  public String MonType;
  public String AI;
  public String DescStr;
  public String Code;
  public boolean enabled;
  public boolean rangedtype;
  public boolean placespawn;
  public String spawn;
  public int spawnx;
  public int spawny;
  public String spawnmode;
  public String minion1;
  public String minion2;
  public boolean SetBoss;
  public boolean BossXfer;
  public int PartyMin;
  public int PartyMax;
  public int MinGrp;
  public int MaxGrp;
  public int sparsePopulate;
  public int Velocity;
  public int Run;
  public int Rarity;
  public String MonSound;
  public String UMonSound;
  public int threat;
  public String MissA1;
  public String MissA2;
  public String MissS1;
  public String MissS2;
  public String MissS3;
  public String MissS4;
  public String MissC;
  public String MissSQ;
  public int Align;
  public boolean isSpawn;
  public boolean isMelee;
  public boolean npc;
  public boolean interact;
  public boolean inventory;
  public boolean inTown;
  public boolean lUndead;
  public boolean hUndead;
  public boolean demon;
  public boolean flying;
  public boolean opendoors;
  public boolean boss;
  public boolean primeevil;
  public boolean killable;
  public boolean switchai;
  public boolean noAura;
  public boolean nomultishot;
  public boolean neverCount;
  public boolean petIgnore;
  public boolean deathDmg;
  public boolean genericSpawn;
  public boolean zoo;
  public int SendSkills;
  public String Skill1;
  public String Sk1mode;
  public int Sk1lvl;
  public String Skill2;
  public String Sk2mode;
  public int Sk2lvl;
  public String Skill3;
  public String Sk3mode;
  public int Sk3lvl;
  public String Skill4;
  public String Sk4mode;
  public int Sk4lvl;
  public String Skill5;
  public String Sk5mode;
  public int Sk5lvl;
  public String Skill6;
  public String Sk6mode;
  public int Sk6lvl;
  public String Skill7;
  public String Sk7mode;
  public int Sk7lvl;
  public String Skill8;
  public String Sk8mode;
  public int Sk8lvl;
  public int DamageRegen;
  public String SkillDamage;
  public boolean noRatio;
  public boolean NoShldBlock;
  public int Crit;
  public String El1Mode;
  public String El1Type;
  public String El2Mode;
  public String El2Type;
  public String El3Mode;
  public String El3Type;
  public int TCQuestId;
  public int TCQuestCP;
  public int SplEndDeath;
  public boolean SplGetModeChart;
  public boolean SplEndGeneric;
  public boolean SplClientEnd;

  @Format(format = "Level%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] Level;
  @Format(format = "aidel%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aidel;
  @Format(format = "aidist%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aidist;
  @Format(format = "aip1%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip1;
  @Format(format = "aip2%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip2;
  @Format(format = "aip3%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip3;
  @Format(format = "aip4%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip4;
  @Format(format = "aip5%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip5;
  @Format(format = "aip6%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip6;
  @Format(format = "aip7%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip7;
  @Format(format = "aip8%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] aip8;
  @Format(format = "Drain%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] Drain;
  @Format(format = "coldeffect%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] coldeffect;
  @Format(format = "ResDm%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResDm;
  @Format(format = "ResMa%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResMa;
  @Format(format = "ResFi%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResFi;
  @Format(format = "ResLi%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResLi;
  @Format(format = "ResCo%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResCo;
  @Format(format = "ResPo%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ResPo;
  @Format(format = "ToBlock%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] ToBlock;
  @Format(format = "minHP%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] minHP;
  @Format(format = "maxHP%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] maxHP;
  @Format(format = "AC%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] AC;
  @Format(format = "Exp%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] Exp;
  @Format(format = "A1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A1MinD;
  @Format(format = "A1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A1MaxD;
  @Format(format = "A1TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A1TH;
  @Format(format = "A2MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A2MinD;
  @Format(format = "A2MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A2MaxD;
  @Format(format = "A2TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] A2TH;
  @Format(format = "S1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] S1MinD;
  @Format(format = "S1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] S1MaxD;
  @Format(format = "S1TH%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] S1TH;
  @Format(format = "El1Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El1Pct;
  @Format(format = "El1MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El1MinD;
  @Format(format = "El1MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El1MaxD;
  @Format(format = "El1Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El1Dur;
  @Format(format = "El2Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El2Pct;
  @Format(format = "El2MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El2MinD;
  @Format(format = "El2MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El2MaxD;
  @Format(format = "El2Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El2Dur;
  @Format(format = "El3Pct%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El3Pct;
  @Format(format = "El3MinD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El3MinD;
  @Format(format = "El3MaxD%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El3MaxD;
  @Format(format = "El3Dur%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] El3Dur;
  @Format(format = "TreasureClass1%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public String[] TreasureClass1;
  @Format(format = "TreasureClass2%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public String[] TreasureClass2;
  @Format(format = "TreasureClass3%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public String[] TreasureClass3;
  @Format(format = "TreasureClass4%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public String[] TreasureClass4;
}
