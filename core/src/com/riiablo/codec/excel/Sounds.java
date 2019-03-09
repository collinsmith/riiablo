package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;

public class Sounds extends Excel<Sounds.Entry> {
  @Override
  protected void put(int id, Entry value) {
    super.put(value.Index, value);
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Sound;
    }

    @Key
    @Column public String  Sound;
    @Column public int     Index;
    @Column public String  FileName;
    @Column public int     Volume;
    @Column(format = "Group Size")
    public int     Group_Size;
    @Column public boolean Loop;
    @Column(format = "Fade In")
    public int     Fade_In;
    @Column(format = "Fade Out")
    public int     Fade_Out;
    @Column(format = "Defer Inst")
    public boolean Defer_Inst;
    @Column(format = "Stop Inst")
    public boolean Stop_Inst;
    @Column public int     Duration;
    @Column public int     Compound;
    @Column public int     Reverb; // Marked boolean, but some rows are int
    @Column public int     Falloff;
    @Column public boolean Cache;
    @Column(format = "Async Only")
    public boolean Async_Only;
    @Column public int     Priority;
    @Column public boolean Stream;
    @Column public boolean Stereo;
    @Column public boolean Tracking;
    @Column public boolean Solo;
    @Column(format = "Music Vol")
    public boolean Music_Vol;
    @Column(format = "Block 1")
    public String  Block_1;
    @Column(format = "Block 2")
    public String  Block_2;
    @Column(format = "Block 3")
    public String  Block_3;

  }
}
