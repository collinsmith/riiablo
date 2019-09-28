package com.riiablo.codec.excel;

@Excel.Binned
public class MonStats extends Excel<MonStats.Entry> {
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

    /*
    @Override
    public void readBin(DataInput in) throws IOException {
      Id = in.readUTF();
      hcIdx = in.readInt();
      BaseId = in.readUTF();
      NextInClass = in.readUTF();
      TransLvl = in.readInt();
      NameStr = in.readUTF();
      MonStatsEx = in.readUTF();
      MonProp = in.readUTF();
      MonType = in.readUTF();
      AI = in.readUTF();
      DescStr = in.readUTF();
      Code = in.readUTF();
      enabled = in.readBoolean();
      rangedtype = in.readBoolean();
      placespawn = in.readBoolean();
      spawn = in.readUTF();
      spawnx = in.readInt();
      spawny = in.readInt();
      spawnmode = in.readUTF();
      minion1 = in.readUTF();
      minion2 = in.readUTF();
      SetBoss = in.readBoolean();
      BossXfer = in.readBoolean();
      PartyMin = in.readInt();
      PartyMax = in.readInt();
      MinGrp = in.readInt();
      MaxGrp = in.readInt();
      sparsePopulate = in.readInt();
      Velocity = in.readInt();
      Run = in.readInt();
      Rarity = in.readInt();
      Level = new int[3];
      for (int x = 0; x < 3; x++) Level[x] = in.readInt();
      MonSound = in.readUTF();
      UMonSound = in.readUTF();
      threat = in.readInt();
      aidel = new int[3];
      for (int x = 0; x < 3; x++) aidel[x] = in.readInt();
      aidist = new int[3];
      for (int x = 0; x < 3; x++) aidist[x] = in.readInt();
      aip1 = new int[3];
      for (int x = 0; x < 3; x++) aip1[x] = in.readInt();
      aip2 = new int[3];
      for (int x = 0; x < 3; x++) aip2[x] = in.readInt();
      aip3 = new int[3];
      for (int x = 0; x < 3; x++) aip3[x] = in.readInt();
      aip4 = new int[3];
      for (int x = 0; x < 3; x++) aip4[x] = in.readInt();
      aip5 = new int[3];
      for (int x = 0; x < 3; x++) aip5[x] = in.readInt();
      aip6 = new int[3];
      for (int x = 0; x < 3; x++) aip6[x] = in.readInt();
      aip7 = new int[3];
      for (int x = 0; x < 3; x++) aip7[x] = in.readInt();
      aip8 = new int[3];
      for (int x = 0; x < 3; x++) aip8[x] = in.readInt();
      MissA1 = in.readUTF();
      MissA2 = in.readUTF();
      MissS1 = in.readUTF();
      MissS2 = in.readUTF();
      MissS3 = in.readUTF();
      MissS4 = in.readUTF();
      MissC = in.readUTF();
      MissSQ = in.readUTF();
      Align = in.readInt();
      isSpawn = in.readBoolean();
      isMelee = in.readBoolean();
      npc = in.readBoolean();
      interact = in.readBoolean();
      inventory = in.readBoolean();
      inTown = in.readBoolean();
      lUndead = in.readBoolean();
      hUndead = in.readBoolean();
      demon = in.readBoolean();
      flying = in.readBoolean();
      opendoors = in.readBoolean();
      boss = in.readBoolean();
      primeevil = in.readBoolean();
      killable = in.readBoolean();
      switchai = in.readBoolean();
      noAura = in.readBoolean();
      nomultishot = in.readBoolean();
      neverCount = in.readBoolean();
      petIgnore = in.readBoolean();
      deathDmg = in.readBoolean();
      genericSpawn = in.readBoolean();
      zoo = in.readBoolean();
      SendSkills = in.readInt();
      Skill1 = in.readUTF();
      Sk1mode = in.readUTF();
      Sk1lvl = in.readInt();
      Skill2 = in.readUTF();
      Sk2mode = in.readUTF();
      Sk2lvl = in.readInt();
      Skill3 = in.readUTF();
      Sk3mode = in.readUTF();
      Sk3lvl = in.readInt();
      Skill4 = in.readUTF();
      Sk4mode = in.readUTF();
      Sk4lvl = in.readInt();
      Skill5 = in.readUTF();
      Sk5mode = in.readUTF();
      Sk5lvl = in.readInt();
      Skill6 = in.readUTF();
      Sk6mode = in.readUTF();
      Sk6lvl = in.readInt();
      Skill7 = in.readUTF();
      Sk7mode = in.readUTF();
      Sk7lvl = in.readInt();
      Skill8 = in.readUTF();
      Sk8mode = in.readUTF();
      Sk8lvl = in.readInt();
      Drain = new int[3];
      for (int x = 0; x < 3; x++) Drain[x] = in.readInt();
      coldeffect = new int[3];
      for (int x = 0; x < 3; x++) coldeffect[x] = in.readInt();
      ResDm = new int[3];
      for (int x = 0; x < 3; x++) ResDm[x] = in.readInt();
      ResMa = new int[3];
      for (int x = 0; x < 3; x++) ResMa[x] = in.readInt();
      ResFi = new int[3];
      for (int x = 0; x < 3; x++) ResFi[x] = in.readInt();
      ResLi = new int[3];
      for (int x = 0; x < 3; x++) ResLi[x] = in.readInt();
      ResCo = new int[3];
      for (int x = 0; x < 3; x++) ResCo[x] = in.readInt();
      ResPo = new int[3];
      for (int x = 0; x < 3; x++) ResPo[x] = in.readInt();
      DamageRegen = in.readInt();
      SkillDamage = in.readUTF();
      noRatio = in.readBoolean();
      NoShldBlock = in.readBoolean();
      ToBlock = new int[3];
      for (int x = 0; x < 3; x++) ToBlock[x] = in.readInt();
      Crit = in.readInt();
      minHP = new int[3];
      for (int x = 0; x < 3; x++) minHP[x] = in.readInt();
      maxHP = new int[3];
      for (int x = 0; x < 3; x++) maxHP[x] = in.readInt();
      AC = new int[3];
      for (int x = 0; x < 3; x++) AC[x] = in.readInt();
      Exp = new int[3];
      for (int x = 0; x < 3; x++) Exp[x] = in.readInt();
      A1MinD = new int[3];
      for (int x = 0; x < 3; x++) A1MinD[x] = in.readInt();
      A1MaxD = new int[3];
      for (int x = 0; x < 3; x++) A1MaxD[x] = in.readInt();
      A1TH = new int[3];
      for (int x = 0; x < 3; x++) A1TH[x] = in.readInt();
      A2MinD = new int[3];
      for (int x = 0; x < 3; x++) A2MinD[x] = in.readInt();
      A2MaxD = new int[3];
      for (int x = 0; x < 3; x++) A2MaxD[x] = in.readInt();
      A2TH = new int[3];
      for (int x = 0; x < 3; x++) A2TH[x] = in.readInt();
      S1MinD = new int[3];
      for (int x = 0; x < 3; x++) S1MinD[x] = in.readInt();
      S1MaxD = new int[3];
      for (int x = 0; x < 3; x++) S1MaxD[x] = in.readInt();
      S1TH = new int[3];
      for (int x = 0; x < 3; x++) S1TH[x] = in.readInt();
      El1Mode = in.readUTF();
      El1Type = in.readUTF();
      El1Pct = new int[3];
      for (int x = 0; x < 3; x++) El1Pct[x] = in.readInt();
      El1MinD = new int[3];
      for (int x = 0; x < 3; x++) El1MinD[x] = in.readInt();
      El1MaxD = new int[3];
      for (int x = 0; x < 3; x++) El1MaxD[x] = in.readInt();
      El1Dur = new int[3];
      for (int x = 0; x < 3; x++) El1Dur[x] = in.readInt();
      El2Mode = in.readUTF();
      El2Type = in.readUTF();
      El2Pct = new int[3];
      for (int x = 0; x < 3; x++) El2Pct[x] = in.readInt();
      El2MinD = new int[3];
      for (int x = 0; x < 3; x++) El2MinD[x] = in.readInt();
      El2MaxD = new int[3];
      for (int x = 0; x < 3; x++) El2MaxD[x] = in.readInt();
      El2Dur = new int[3];
      for (int x = 0; x < 3; x++) El2Dur[x] = in.readInt();
      El3Mode = in.readUTF();
      El3Type = in.readUTF();
      El3Pct = new int[3];
      for (int x = 0; x < 3; x++) El3Pct[x] = in.readInt();
      El3MinD = new int[3];
      for (int x = 0; x < 3; x++) El3MinD[x] = in.readInt();
      El3MaxD = new int[3];
      for (int x = 0; x < 3; x++) El3MaxD[x] = in.readInt();
      El3Dur = new int[3];
      for (int x = 0; x < 3; x++) El3Dur[x] = in.readInt();
      TreasureClass1 = new String[3];
      for (int x = 0; x < 3; x++) TreasureClass1[x] = in.readUTF();
      TreasureClass2 = new String[3];
      for (int x = 0; x < 3; x++) TreasureClass2[x] = in.readUTF();
      TreasureClass3 = new String[3];
      for (int x = 0; x < 3; x++) TreasureClass3[x] = in.readUTF();
      TreasureClass4 = new String[3];
      for (int x = 0; x < 3; x++) TreasureClass4[x] = in.readUTF();
      TCQuestId = in.readInt();
      TCQuestCP = in.readInt();
      SplEndDeath = in.readInt();
      SplGetModeChart = in.readBoolean();
      SplEndGeneric = in.readBoolean();
      SplClientEnd = in.readBoolean();
    }

    @Override
    public void writeBin(DataOutput out) throws IOException {
      out.writeUTF(Id);
      out.writeInt(hcIdx);
      out.writeUTF(BaseId);
      out.writeUTF(NextInClass);
      out.writeInt(TransLvl);
      out.writeUTF(NameStr);
      out.writeUTF(MonStatsEx);
      out.writeUTF(MonProp);
      out.writeUTF(MonType);
      out.writeUTF(AI);
      out.writeUTF(DescStr);
      out.writeUTF(Code);
      out.writeBoolean(enabled);
      out.writeBoolean(rangedtype);
      out.writeBoolean(placespawn);
      out.writeUTF(spawn);
      out.writeInt(spawnx);
      out.writeInt(spawny);
      out.writeUTF(spawnmode);
      out.writeUTF(minion1);
      out.writeUTF(minion2);
      out.writeBoolean(SetBoss);
      out.writeBoolean(BossXfer);
      out.writeInt(PartyMin);
      out.writeInt(PartyMax);
      out.writeInt(MinGrp);
      out.writeInt(MaxGrp);
      out.writeInt(sparsePopulate);
      out.writeInt(Velocity);
      out.writeInt(Run);
      out.writeInt(Rarity);
      for (int x : Level) out.writeInt(x);
      out.writeUTF(MonSound);
      out.writeUTF(UMonSound);
      out.writeInt(threat);
      for (int x : aidel) out.writeInt(x);
      for (int x : aidist) out.writeInt(x);
      for (int x : aip1) out.writeInt(x);
      for (int x : aip2) out.writeInt(x);
      for (int x : aip3) out.writeInt(x);
      for (int x : aip4) out.writeInt(x);
      for (int x : aip5) out.writeInt(x);
      for (int x : aip6) out.writeInt(x);
      for (int x : aip7) out.writeInt(x);
      for (int x : aip8) out.writeInt(x);
      out.writeUTF(MissA1);
      out.writeUTF(MissA2);
      out.writeUTF(MissS1);
      out.writeUTF(MissS2);
      out.writeUTF(MissS3);
      out.writeUTF(MissS4);
      out.writeUTF(MissC);
      out.writeUTF(MissSQ);
      out.writeInt(Align);
      out.writeBoolean(isSpawn);
      out.writeBoolean(isMelee);
      out.writeBoolean(npc);
      out.writeBoolean(interact);
      out.writeBoolean(inventory);
      out.writeBoolean(inTown);
      out.writeBoolean(lUndead);
      out.writeBoolean(hUndead);
      out.writeBoolean(demon);
      out.writeBoolean(flying);
      out.writeBoolean(opendoors);
      out.writeBoolean(boss);
      out.writeBoolean(primeevil);
      out.writeBoolean(killable);
      out.writeBoolean(switchai);
      out.writeBoolean(noAura);
      out.writeBoolean(nomultishot);
      out.writeBoolean(neverCount);
      out.writeBoolean(petIgnore);
      out.writeBoolean(deathDmg);
      out.writeBoolean(genericSpawn);
      out.writeBoolean(zoo);
      out.writeInt(SendSkills);
      out.writeUTF(Skill1);
      out.writeUTF(Sk1mode);
      out.writeInt(Sk1lvl);
      out.writeUTF(Skill2);
      out.writeUTF(Sk2mode);
      out.writeInt(Sk2lvl);
      out.writeUTF(Skill3);
      out.writeUTF(Sk3mode);
      out.writeInt(Sk3lvl);
      out.writeUTF(Skill4);
      out.writeUTF(Sk4mode);
      out.writeInt(Sk4lvl);
      out.writeUTF(Skill5);
      out.writeUTF(Sk5mode);
      out.writeInt(Sk5lvl);
      out.writeUTF(Skill6);
      out.writeUTF(Sk6mode);
      out.writeInt(Sk6lvl);
      out.writeUTF(Skill7);
      out.writeUTF(Sk7mode);
      out.writeInt(Sk7lvl);
      out.writeUTF(Skill8);
      out.writeUTF(Sk8mode);
      out.writeInt(Sk8lvl);
      for (int x : Drain) out.writeInt(x);
      for (int x : coldeffect) out.writeInt(x);
      for (int x : ResDm) out.writeInt(x);
      for (int x : ResMa) out.writeInt(x);
      for (int x : ResFi) out.writeInt(x);
      for (int x : ResLi) out.writeInt(x);
      for (int x : ResCo) out.writeInt(x);
      for (int x : ResPo) out.writeInt(x);
      out.writeInt(DamageRegen);
      out.writeUTF(SkillDamage);
      out.writeBoolean(noRatio);
      out.writeBoolean(NoShldBlock);
      for (int x : ToBlock) out.writeInt(x);
      out.writeInt(Crit);
      for (int x : minHP) out.writeInt(x);
      for (int x : maxHP) out.writeInt(x);
      for (int x : AC) out.writeInt(x);
      for (int x : Exp) out.writeInt(x);
      for (int x : A1MinD) out.writeInt(x);
      for (int x : A1MaxD) out.writeInt(x);
      for (int x : A1TH) out.writeInt(x);
      for (int x : A2MinD) out.writeInt(x);
      for (int x : A2MaxD) out.writeInt(x);
      for (int x : A2TH) out.writeInt(x);
      for (int x : S1MinD) out.writeInt(x);
      for (int x : S1MaxD) out.writeInt(x);
      for (int x : S1TH) out.writeInt(x);
      out.writeUTF(El1Mode);
      out.writeUTF(El1Type);
      for (int x : El1Pct) out.writeInt(x);
      for (int x : El1MinD) out.writeInt(x);
      for (int x : El1MaxD) out.writeInt(x);
      for (int x : El1Dur) out.writeInt(x);
      out.writeUTF(El2Mode);
      out.writeUTF(El2Type);
      for (int x : El2Pct) out.writeInt(x);
      for (int x : El2MinD) out.writeInt(x);
      for (int x : El2MaxD) out.writeInt(x);
      for (int x : El2Dur) out.writeInt(x);
      out.writeUTF(El3Mode);
      out.writeUTF(El3Type);
      for (int x : El3Pct) out.writeInt(x);
      for (int x : El3MinD) out.writeInt(x);
      for (int x : El3MaxD) out.writeInt(x);
      for (int x : El3Dur) out.writeInt(x);
      for (String x : TreasureClass1) out.writeUTF(x);
      for (String x : TreasureClass2) out.writeUTF(x);
      for (String x : TreasureClass3) out.writeUTF(x);
      for (String x : TreasureClass4) out.writeUTF(x);
      out.writeInt(TCQuestId);
      out.writeInt(TCQuestCP);
      out.writeInt(SplEndDeath);
      out.writeBoolean(SplGetModeChart);
      out.writeBoolean(SplEndGeneric);
      out.writeBoolean(SplClientEnd);
    }
    */
  }
}
