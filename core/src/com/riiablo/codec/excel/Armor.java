package com.riiablo.codec.excel;

import com.riiablo.codec.excel.Excel;
import com.riiablo.codec.excel.ItemEntry;

public class Armor extends Excel<Armor.Entry> {
  public static class Entry extends ItemEntry {
    @Column public int     Torso;
    @Column public int     Legs;
    @Column public int     rArm;
    @Column public int     lArm;
    @Column public int     lSPad;
    @Column public int     rSPad;
  }
}
