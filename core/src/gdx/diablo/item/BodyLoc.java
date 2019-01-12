package gdx.diablo.item;

import com.badlogic.gdx.Gdx;

public enum BodyLoc {
  NONE,
  HEAD,
  NECK,
  TORS,
  RARM,
  LARM,
  RRIN,
  LRIN,
  BELT,
  FEET,
  GLOV,
  RARM2,
  LARM2;

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
}
