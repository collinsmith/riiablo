package com.riiablo.codec.excel;

@Excel.Binned
public class Overlay extends Excel<Overlay.Entry> {
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return overlay;
    }

    @Key
    @Column
    public String  overlay;
    @Column public String  Filename;
    @Column public int     version;
    @Column public int     Frames;
    @Column public String  Character;
    @Column public boolean PreDraw;
    @Column(format = "1ofN")
    public int     _1ofN;
    @Column public int     Dir;
    @Column public boolean Open;
    @Column public boolean Beta;
    @Column public int     Xoffset;
    @Column public int     Yoffset;
    @Column public int     Height1;
    @Column public int     Height2;
    @Column public int     Height3;
    @Column public int     Height4;
    @Column public int     AnimRate;
    @Column public int     LoopWaitTime;
    @Column public int     Trans;
    @Column public int     InitRadius;
    @Column public int     Radius;
    @Column public int     Red;
    @Column public int     Green;
    @Column public int     Blue;
    @Column public int     NumDirections;
    @Column public boolean LocalBlood;
  }
}
