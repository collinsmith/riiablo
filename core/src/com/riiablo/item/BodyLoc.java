package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.riiablo.engine.Dirty;

import static com.riiablo.engine.Dirty.HD;
import static com.riiablo.engine.Dirty.LA;
import static com.riiablo.engine.Dirty.LG;
import static com.riiablo.engine.Dirty.LH;
import static com.riiablo.engine.Dirty.RA;
import static com.riiablo.engine.Dirty.RH;
import static com.riiablo.engine.Dirty.S1;
import static com.riiablo.engine.Dirty.S2;
import static com.riiablo.engine.Dirty.SH;
import static com.riiablo.engine.Dirty.TR;

public enum BodyLoc {
  NONE,
  HEAD(HD),
  NECK,
  TORS(TR|LG|LA|RA|S1|S2),
  RARM(RH|LH|SH),
  LARM(RH|LH|SH),
  RRIN,
  LRIN,
  BELT,
  FEET(LG),
  GLOV,
  RARM2(RH|LH|SH),
  LARM2(RH|LH|SH);

  public static BodyLoc getAlternate(BodyLoc bodyLoc, int i) {
    switch (bodyLoc) {
      case RARM:
      case RARM2:
        return i > 0 ? RARM2 : RARM;
      case LARM:
      case LARM2:
        return i > 0 ? LARM2 : LARM;
      default:
        return bodyLoc;
    }
  }

  public static boolean isWeaponLoc(BodyLoc bodyLoc) {
    switch (bodyLoc) {
      case RARM: case LARM:
      case RARM2: case LARM2:
        return true;
      default:
        return false;
    }
  }

  public static BodyLoc valueOf(int i) {
    switch (i) {
      case 0:  return NONE;
      case 1:  return HEAD;
      case 2:  return NECK;
      case 3:  return TORS;
      case 4:  return RARM;
      case 5:  return LARM;
      case 6:  return RRIN;
      case 7:  return LRIN;
      case 8:  return BELT;
      case 9:  return FEET;
      case 10: return GLOV;
      case 11: return RARM2;
      case 12: return LARM2;
      default:
        Gdx.app.error("BodyLoc", "Unknown body location: " + i);
        return null;
    }
  }

  final int components;

  BodyLoc() {
    this.components = Dirty.NONE;
  }

  BodyLoc(int components) {
    this.components = components;
  }

  public int components() {
    return components;
  }

  public boolean contains(int component) {
    return components != 0 && (components & (1 << component)) != 0;
  }
}
