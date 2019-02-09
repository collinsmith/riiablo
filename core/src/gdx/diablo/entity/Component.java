package gdx.diablo.entity;

import com.badlogic.gdx.Gdx;

public enum Component {
  HD,
  TR,
  LG,
  RA,
  LA,
  RH,
  LH,
  SH,
  S1,
  S2,
  S3,
  S4,
  S5,
  S6,
  S7,
  S8;

  public static Component valueOf(int i) {
    switch (i) {
      case 0x0: return HD;
      case 0x1: return TR;
      case 0x2: return LG;
      case 0x3: return RA;
      case 0x4: return LA;
      case 0x5: return RH;
      case 0x6: return LH;
      case 0x7: return SH;
      case 0x8: return S1;
      case 0x9: return S2;
      case 0xA: return S3;
      case 0xB: return S4;
      case 0xC: return S5;
      case 0xD: return S6;
      case 0xE: return S7;
      case 0xF: return S8;
      default:
        Gdx.app.error("Component", "Unknown component: " + i);
        return null;
    }
  }
}
