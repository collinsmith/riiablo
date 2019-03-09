package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class Objects extends Excel<Objects.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Description;
    }

    private static final int NUM_MODES = 8;

    @Column public String  Name;
    @Column(format = "description - not loaded")
    public String  Description;
    @Column
    @Key
    public int     Id;
    @Column public String  Token;
    @Column public int     SpawnMax;
    @Column(endIndex = NUM_MODES) public boolean Selectable[];
    @Column public int     TrapProb;
    @Column public int     SizeX, SizeY;
    @Column public int     nTgtFX, nTgtFY;
    @Column public int     nTgtBX, nTgtBY;
    @Column(endIndex = NUM_MODES) public int     FrameCnt[];
    @Column(endIndex = NUM_MODES) public int     FrameDelta[];
    @Column(endIndex = NUM_MODES) public boolean CycleAnim[];
    @Column(endIndex = NUM_MODES) public int     Lit[];
    @Column(endIndex = NUM_MODES) public boolean BlocksLight[];
    @Column(endIndex = NUM_MODES) public boolean HasCollision[];
    @Column public boolean IsAttackable0;
    @Column(endIndex = NUM_MODES) public int     Start[];
    @Column public boolean EnvEffect;
    @Column public boolean IsDoor;
    @Column public boolean BlocksVis;
    @Column public int     Orientation;
    @Column public int     Trans;
    @Column(endIndex = NUM_MODES) public int     OrderFlag[];
    @Column public boolean PreOperate;
    @Column(endIndex = NUM_MODES) public boolean Mode[];
    @Column public int     Yoffset, Xoffset;
    @Column public boolean Draw;
    @Column public int     Red, Green, Blue;
    @Column(values = {
        "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
    })
    public boolean Components[];
    @Column public int     TotalPieces;
    @Column public int     SubClass;
    @Column public int     Xspace, Yspace;
    @Column public int     NameOffset;
    @Column public boolean MonsterOK;
    @Column public int     OperateRange;
    @Column public int     ShrineFunction;
    @Column public boolean Restore;
    @Column(endIndex = 8)
    public int     Parm[];
    @Column public int     Act;
    @Column public boolean Lockable;
    @Column public boolean Gore;
    @Column public boolean Sync;
    @Column public boolean Flicker;
    @Column public int     Damage;
    @Column public boolean Beta;
    @Column public int     Overlay;
    @Column public boolean CollisionSubst;
    @Column public int     Left, Top, Width, Height;
    @Column public int     OperateFn;
    @Column public int     PopulateFn;
    @Column public int     InitFn;
    @Column public int     ClientFn;
    @Column public boolean RestoreVirgins;
    @Column public boolean BlockMissile;
    @Column public int     DrawUnder;
    @Column public boolean OpenWarp;
    @Column public int     AutoMap;
  }
}
