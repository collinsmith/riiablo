package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Objects {
  @Override
  public String toString() {
    return Description;
  }

  private static final int NUM_MODES = 8;

  public String Name;

  @Format(format = "description - not loaded")
  public String Description;

  @PrimaryKey
  public int Id;

  public String Token;
  public int SpawnMax;

  @Format(endIndex = NUM_MODES)
  public boolean Selectable[];

  public int TrapProb;
  public int SizeX, SizeY;
  public int nTgtFX, nTgtFY;
  public int nTgtBX, nTgtBY;

  @Format(endIndex = NUM_MODES)
  public int FrameCnt[];

  @Format(endIndex = NUM_MODES)
  public int FrameDelta[];

  @Format(endIndex = NUM_MODES)
  public boolean CycleAnim[];

  @Format(endIndex = NUM_MODES)
  public int Lit[];

  @Format(endIndex = NUM_MODES)
  public boolean BlocksLight[];

  @Format(endIndex = NUM_MODES)
  public boolean HasCollision[];

  public boolean IsAttackable0;

  @Format(endIndex = NUM_MODES)
  public int Start[];

  public boolean EnvEffect;
  public boolean IsDoor;
  public boolean BlocksVis;
  public int Orientation;
  public int Trans;

  @Format(endIndex = NUM_MODES)
  public int OrderFlag[];

  public boolean PreOperate;

  @Format(endIndex = NUM_MODES)
  public boolean Mode[];

  public int Yoffset, Xoffset;
  public boolean Draw;
  public int Red, Green, Blue;

  @Format(
      endIndex = 16,
      values = {
          "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
      })
  public boolean Components[];

  public int TotalPieces;
  public int SubClass;
  public int Xspace, Yspace;
  public int NameOffset;
  public boolean MonsterOK;
  public int OperateRange;
  public int ShrineFunction;
  public boolean Restore;

  @Format(endIndex = 8)
  public int Parm[];

  public int Act;
  public boolean Lockable;
  public boolean Gore;
  public boolean Sync;
  public boolean Flicker;
  public int Damage;
  public boolean Beta;
  public int Overlay;
  public boolean CollisionSubst;
  public int Left, Top, Width, Height;
  public int OperateFn;
  public int PopulateFn;
  public int InitFn;
  public int ClientFn;
  public boolean RestoreVirgins;
  public boolean BlockMissile;
  public int DrawUnder;
  public boolean OpenWarp;
  public int AutoMap;
}
