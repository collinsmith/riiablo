package com.riiablo.excel.txt;

import com.riiablo.excel.annotation.Format;
import com.riiablo.excel.annotation.PrimaryKey;
import com.riiablo.excel.annotation.Schema;

@Schema
// @Table(MonStatsTableImpl.class)
// @SerializedWith(MonStatsSerializerImpl.class)
@SuppressWarnings("unused")
public class MonStats {
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

  @Format(format = "Level%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int[] Level;


}
