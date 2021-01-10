package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Missiles {
  @Override
  public String toString() {
    return Missile;
  }

  @PrimaryKey
  public String Missile;
  public int Id;
  public int pCltDoFunc;
  public int pCltHitFunc;
  public int pSrvDoFunc;
  public int pSrvHitFunc;
  public int pSrvDmgFunc;
  public String SrvCalc1;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public int Param[];
  public String CltCalc1;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public int CltParam[];

  public String SHitCalc1;

  @Format(
      startIndex = 1,
      endIndex = 4)
  public int sHitPar[];

  public String CHitCalc1;

  @Format(
      startIndex = 1,
      endIndex = 4)
  public int cHitPar[];

  public String DmgCalc1;

  @Format(
      startIndex = 1,
      endIndex = 3)
  public int dParam[];

  public int Vel;
  public int MaxVel;
  public int VelLev;
  public int Accel;
  public int Range;
  public int LevRange;
  public int Light;
  public int Flicker;
  public int Red;
  public int Green;
  public int Blue;
  public int InitSteps;
  public int Activate;
  public int LoopAnim;
  public String CelFile;
  public int animrate;
  public int AnimLen;
  public int AnimSpeed;
  public int RandStart;
  public int SubLoop;
  public int SubStart;
  public int SubStop;
  public int CollideType;
  public boolean CollideKill;
  public boolean CollideFriend;
  public boolean LastCollide;
  public boolean Collision;
  public boolean ClientCol;
  public boolean ClientSend;
  public boolean NextHit;
  public int NextDelay;
  public int xoffset;
  public int yoffset;
  public int zoffset;
  public int Size;
  public boolean SrcTown;
  public int CltSrcTown;
  public boolean CanDestroy;
  public boolean ToHit;
  public boolean AlwaysExplode;
  public int Explosion;
  public boolean Town;
  public boolean NoUniqueMod;
  public int NoMultiShot;
  public int Holy;
  public boolean CanSlow;
  public boolean ReturnFire;
  public boolean GetHit;
  public boolean SoftHit;
  public int KnockBack;
  public int Trans;
  public boolean Qty;
  public boolean Pierce;
  public boolean SpecialSetup;
  public boolean MissileSkill;
  public String Skill;
  public int ResultFlags;
  public int HitFlags;
  public int HitShift;
  public boolean ApplyMastery;
  public int SrcDamage;
  public boolean Half2HSrc;
  public int SrcMissDmg;
  public int MinDamage;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public String MinLevDam[];

  public String MaxDamage;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public int MaxLevDam[];

  public String DmgSymPerCalc;
  public String EType;
  public int EMin;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public int MinELev[];

  public String Emax;

  @Format(
      startIndex = 1,
      endIndex = 6)
  public String MaxELev[];

  public String EDmgSymPerCalc;
  public int ELen;

  @Format(
      startIndex = 1,
      endIndex = 4)
  public int ELevLen[];

  public int HitClass;
  public int NumDirections;
  public boolean LocalBlood;
  public int DamageRate;
  public String TravelSound;
  public String HitSound;
  public String ProgSound;
  public String ProgOverlay;
  public String ExplosionMissile;

  @Format(
      startIndex = 1,
      endIndex = 4)
  public String SubMissile[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String HitSubMissile[];

  @Format(
      startIndex = 1,
      endIndex = 4)
  public String CltSubMissile[];

  @Format(
      startIndex = 1,
      endIndex = 5)
  public String CltHitSubMissile[];
}
