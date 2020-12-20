package com.riiablo.table.schema;

import com.riiablo.table.annotation.Format;
import com.riiablo.table.annotation.PrimaryKey;
import com.riiablo.table.annotation.Schema;

@Schema
@SuppressWarnings("unused")
public class MonStats2 {
  @Override
  public String toString() {
    return Id;
  }

  @PrimaryKey
  public String Id;
  public int Height;
  public int OverlayHeight;
  public int pixHeight;
  public int SizeX;
  public int SizeY;
  public int spawnCol;
  public int MeleeRng;
  public String BaseW;
  public int HitClass;

  @Format(format = "%sv", endIndex = 16, values = {
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
  })
  public String ComponentV[];

  @Format(endIndex = 16, values = {
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"
  })
  public boolean Components[];

  public int TotalPieces;

  @Format(format = "m%s", endIndex = 16, values = {
      "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
  })
  public boolean mMode[];

  @Format(format = "d%s", endIndex = 16, values = {
      "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
  })
  public int dMode[];

  @Format(format = "%smv", endIndex = 16, values = {
      "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "KB", "SQ", "RN"
  })
  public boolean Modemv[];

  //public int A1mv;
  //public int A2mv;
  //public int SCmv;
  //public int S1mv;
  //public int S2mv;
  //public int S3mv;
  //public int S4mv;
  public boolean noGfxHitTest;
  public int htTop;
  public int htLeft;
  public int htWidth;
  public int htHeight;
  public int restore;
  public int automapCel;
  public boolean noMap;
  public boolean noOvly;
  public boolean isSel;
  public boolean alSel;
  public boolean noSel;
  public boolean shiftSel;
  public boolean corpseSel;
  public boolean isAtt;
  public boolean revive;
  public boolean critter;
  public boolean small;
  public boolean large;
  public boolean soft;
  public boolean inert;
  public boolean objCol;
  public boolean deadCol;
  public boolean unflatDead;
  public boolean Shadow;
  public boolean noUniqueShift;
  public boolean compositeDeath;
  public int localBlood;
  public int Bleed;
  public int Light;

  @Format(format = "light-%s", values = {"r", "g", "b"}, endIndex = 3)
  public int light[];

  @Format(format = "Utrans%s", values = {"", "(N)", "(H)"}, endIndex = 3)
  public int Utrans[];

  public String Heart;
  public String BodyPart;
  public int InfernoLen;
  public int InfernoAnim;
  public int InfernoRollback;
  public String ResurrectMode;
  public String ResurrectSkill;
}
