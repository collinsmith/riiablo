package com.riiablo.codec.excel;

@Excel.Binned
public class Missiles extends Excel<Missiles.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Missile;
    }

    @Key
    @Column
    public String  Missile;
    @Column public int     Id;
    @Column public int     pCltDoFunc;
    @Column public int     pCltHitFunc;
    @Column public int     pSrvDoFunc;
    @Column public int     pSrvHitFunc;
    @Column public int     pSrvDmgFunc;
    @Column public String  SrvCalc1;
    @Column(startIndex = 1, endIndex = 6)
    public int     Param[];
    @Column public String  CltCalc1;
    @Column(startIndex = 1, endIndex = 6)
    public int     CltParam[];
    @Column public String  SHitCalc1;
    @Column(startIndex = 1, endIndex = 4)
    public int     sHitPar[];
    @Column public String  CHitCalc1;
    @Column(startIndex = 1, endIndex = 4)
    public int     cHitPar[];
    @Column public String  DmgCalc1;
    @Column(startIndex = 1, endIndex = 3)
    public int     dParam[];
    @Column public int     Vel;
    @Column public int     MaxVel;
    @Column public int     VelLev;
    @Column public int     Accel;
    @Column public int     Range;
    @Column public int     LevRange;
    @Column public int     Light;
    @Column public int     Flicker;
    @Column public int     Red;
    @Column public int     Green;
    @Column public int     Blue;
    @Column public int     InitSteps;
    @Column public int     Activate;
    @Column public int     LoopAnim;
    @Column public String  CelFile;
    @Column public int     animrate;
    @Column public int     AnimLen;
    @Column public int     AnimSpeed;
    @Column public int     RandStart;
    @Column public int     SubLoop;
    @Column public int     SubStart;
    @Column public int     SubStop;
    @Column public int     CollideType;
    @Column public boolean CollideKill;
    @Column public boolean CollideFriend;
    @Column public boolean LastCollide;
    @Column public boolean Collision;
    @Column public boolean ClientCol;
    @Column public boolean ClientSend;
    @Column public boolean NextHit;
    @Column public int     NextDelay;
    @Column public int     xoffset;
    @Column public int     yoffset;
    @Column public int     zoffset;
    @Column public int     Size;
    @Column public boolean SrcTown;
    @Column public int     CltSrcTown;
    @Column public boolean CanDestroy;
    @Column public boolean ToHit;
    @Column public boolean AlwaysExplode;
    @Column public int     Explosion;
    @Column public boolean Town;
    @Column public boolean NoUniqueMod;
    @Column public int     NoMultiShot;
    @Column public int     Holy;
    @Column public boolean CanSlow;
    @Column public boolean ReturnFire;
    @Column public boolean GetHit;
    @Column public boolean SoftHit;
    @Column public int     KnockBack;
    @Column public int     Trans;
    @Column public boolean Qty;
    @Column public boolean Pierce;
    @Column public boolean SpecialSetup;
    @Column public boolean MissileSkill;
    @Column public String  Skill;
    @Column public int     ResultFlags;
    @Column public int     HitFlags;
    @Column public int     HitShift;
    @Column public boolean ApplyMastery;
    @Column public int     SrcDamage;
    @Column public boolean Half2HSrc;
    @Column public int     SrcMissDmg;
    @Column public int     MinDamage;
    @Column(startIndex = 1, endIndex = 6)
    public String  MinLevDam[];
    @Column public String  MaxDamage;
    @Column(startIndex = 1, endIndex = 6)
    public int     MaxLevDam[];
    @Column public String  DmgSymPerCalc;
    @Column public String  EType;
    @Column public int     EMin;
    @Column(startIndex = 1, endIndex = 6)
    public int     MinELev[];
    @Column public String  Emax;
    @Column(startIndex = 1, endIndex = 6)
    public String  MaxELev[];
    @Column public String  EDmgSymPerCalc;
    @Column public int     ELen;
    @Column(startIndex = 1, endIndex = 4)
    public int     ELevLen[];
    @Column public int     HitClass;
    @Column public int     NumDirections;
    @Column public boolean LocalBlood;
    @Column public int     DamageRate;
    @Column public String  TravelSound;
    @Column public String  HitSound;
    @Column public String  ProgSound;
    @Column public String  ProgOverlay;
    @Column public String  ExplosionMissile;
    @Column(startIndex = 1, endIndex = 4)
    public String  SubMissile[];
    @Column(startIndex = 1, endIndex = 5)
    public String  HitSubMissile[];
    @Column(startIndex = 1, endIndex = 4)
    public String  CltSubMissile[];
    @Column(startIndex = 1, endIndex = 5)
    public String  CltHitSubMissile[];
  }
}
