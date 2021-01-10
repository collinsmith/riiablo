package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class Inventory {
  // TODO:
  // public Entry getClass(int classId) {
  //   switch (classId) {
  //     case 0:
  //     case 1:
  //     case 2:
  //     case 3:
  //     case 4:  return get(classId);
  //     case 5:
  //     case 6:  return get(classId + 15);
  //     default: throw new GdxRuntimeException("Invalid class id: " + classId);
  //   }
  // }

  @Override
  public String toString() {
    return _class;
  }

  @PrimaryKey
  @Format(format = "class")
  public String _class;

  public int invLeft;
  public int invRight;
  public int invTop;
  public int invBottom;
  public int gridX;
  public int gridY;
  public int gridLeft;
  public int gridRight;
  public int gridTop;
  public int gridBottom;
  public int gridBoxWidth;
  public int gridBoxHeight;
  public int rArmLeft;
  public int rArmRight;
  public int rArmTop;
  public int rArmBottom;
  public int rArmWidth;
  public int rArmHeight;
  public int torsoLeft;
  public int torsoRight;
  public int torsoTop;
  public int torsoBottom;
  public int torsoWidth;
  public int torsoHeight;
  public int lArmLeft;
  public int lArmRight;
  public int lArmTop;
  public int lArmBottom;
  public int lArmWidth;
  public int lArmHeight;
  public int headLeft;
  public int headRight;
  public int headTop;
  public int headBottom;
  public int headWidth;
  public int headHeight;
  public int neckLeft;
  public int neckRight;
  public int neckTop;
  public int neckBottom;
  public int neckWidth;
  public int neckHeight;
  public int rHandLeft;
  public int rHandRight;
  public int rHandTop;
  public int rHandBottom;
  public int rHandWidth;
  public int rHandHeight;
  public int lHandLeft;
  public int lHandRight;
  public int lHandTop;
  public int lHandBottom;
  public int lHandWidth;
  public int lHandHeight;
  public int beltLeft;
  public int beltRight;
  public int beltTop;
  public int beltBottom;
  public int beltWidth;
  public int beltHeight;
  public int feetLeft;
  public int feetRight;
  public int feetTop;
  public int feetBottom;
  public int feetWidth;
  public int feetHeight;
  public int glovesLeft;
  public int glovesRight;
  public int glovesTop;
  public int glovesBottom;
  public int glovesWidth;
  public int glovesHeight;
}
