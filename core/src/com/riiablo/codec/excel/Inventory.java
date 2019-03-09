package com.riiablo.codec.excel;

import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.codec.excel.Excel;

public class Inventory extends Excel<Inventory.Entry> {
  public Entry getClass(int classId) {
    switch (classId) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:  return get(classId);
      case 5:
      case 6:  return get(classId + 15);
      default: throw new GdxRuntimeException("Invalid class id: " + classId);
    }
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return _class;
    }

    @Key
    @Column(format = "class")
    public String  _class;
    @Column public int     invLeft;
    @Column public int     invRight;
    @Column public int     invTop;
    @Column public int     invBottom;
    @Column public int     gridX;
    @Column public int     gridY;
    @Column public int     gridLeft;
    @Column public int     gridRight;
    @Column public int     gridTop;
    @Column public int     gridBottom;
    @Column public int     gridBoxWidth;
    @Column public int     gridBoxHeight;
    @Column public int     rArmLeft;
    @Column public int     rArmRight;
    @Column public int     rArmTop;
    @Column public int     rArmBottom;
    @Column public int     rArmWidth;
    @Column public int     rArmHeight;
    @Column public int     torsoLeft;
    @Column public int     torsoRight;
    @Column public int     torsoTop;
    @Column public int     torsoBottom;
    @Column public int     torsoWidth;
    @Column public int     torsoHeight;
    @Column public int     lArmLeft;
    @Column public int     lArmRight;
    @Column public int     lArmTop;
    @Column public int     lArmBottom;
    @Column public int     lArmWidth;
    @Column public int     lArmHeight;
    @Column public int     headLeft;
    @Column public int     headRight;
    @Column public int     headTop;
    @Column public int     headBottom;
    @Column public int     headWidth;
    @Column public int     headHeight;
    @Column public int     neckLeft;
    @Column public int     neckRight;
    @Column public int     neckTop;
    @Column public int     neckBottom;
    @Column public int     neckWidth;
    @Column public int     neckHeight;
    @Column public int     rHandLeft;
    @Column public int     rHandRight;
    @Column public int     rHandTop;
    @Column public int     rHandBottom;
    @Column public int     rHandWidth;
    @Column public int     rHandHeight;
    @Column public int     lHandLeft;
    @Column public int     lHandRight;
    @Column public int     lHandTop;
    @Column public int     lHandBottom;
    @Column public int     lHandWidth;
    @Column public int     lHandHeight;
    @Column public int     beltLeft;
    @Column public int     beltRight;
    @Column public int     beltTop;
    @Column public int     beltBottom;
    @Column public int     beltWidth;
    @Column public int     beltHeight;
    @Column public int     feetLeft;
    @Column public int     feetRight;
    @Column public int     feetTop;
    @Column public int     feetBottom;
    @Column public int     feetWidth;
    @Column public int     feetHeight;
    @Column public int     glovesLeft;
    @Column public int     glovesRight;
    @Column public int     glovesTop;
    @Column public int     glovesBottom;
    @Column public int     glovesWidth;
    @Column public int     glovesHeight;
  }
}
