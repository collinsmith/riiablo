package com.riiablo.item;

import com.badlogic.gdx.Gdx;
import com.riiablo.entity.Entity;

import static gdx.diablo.entity.Entity.Dirty.HD;
import static gdx.diablo.entity.Entity.Dirty.LA;
import static gdx.diablo.entity.Entity.Dirty.LG;
import static gdx.diablo.entity.Entity.Dirty.LH;
import static gdx.diablo.entity.Entity.Dirty.RA;
import static gdx.diablo.entity.Entity.Dirty.RH;
import static gdx.diablo.entity.Entity.Dirty.S1;
import static gdx.diablo.entity.Entity.Dirty.S2;
import static gdx.diablo.entity.Entity.Dirty.SH;
import static gdx.diablo.entity.Entity.Dirty.TR;

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
    this.components = Entity.Dirty.NONE;
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
