package gdx.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.COFD2;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.excel.Armor;

public class Char {
  private static final String TAG = "Char";
  private static final boolean DEBUG = true;

  private static final String CHARS = "data\\global\\chars\\";

  private final CharClass charClass;

  public int x;
  public int y;

  public String cofName;
  public COF cof;

  public String plrType;
  public String plrMode;
  public String weaponClass;
  public String armType;

  public float angle;
  public Animation.COFAnimation animation;

  public Char(CharClass charClass) {
    this.charClass = charClass;

    plrType = PlrType.valueOf(charClass);
    plrMode = PlrMode.PM_TN;
    weaponClass = WeaponClass.WC_1HS;
    update();
  }

  public void setMode(String mode) {
    if (!mode.equalsIgnoreCase(plrMode)) {
      plrMode = mode;
      update();
    }
  }

  public void draw(Batch batch, int x, int y) {
    animation.act();
    animation.draw(batch, x, y);
  }

  public void setAngle(float rad) {
    if (rad < 0) {
      rad = MathUtils.PI + (MathUtils.PI + rad);
    }

    int deg = (int) (MathUtils.radiansToDegrees * rad);

    int d;
    float degPerDir = 360f / 16;
    float angle = degPerDir / 2;
    if (deg < angle) {
      d = 7;
    } else if (deg < (angle += degPerDir)) {
      d = 13;
    } else if (deg < (angle += degPerDir)) {
      d = 2;
    } else if (deg < (angle += degPerDir)) {
      d = 12;
    } else if (deg < (angle += degPerDir)) {
      d = 6;
    } else if (deg < (angle += degPerDir)) {
      d = 11;
    } else if (deg < (angle += degPerDir)) {
      d = 1;
    } else if (deg < (angle += degPerDir)) {
      d = 10;
    } else if (deg < (angle += degPerDir)) {
      d = 5;
    } else if (deg < (angle += degPerDir)) {
      d = 9;
    } else if (deg < (angle += degPerDir)) {
      d = 0;
    } else if (deg < (angle += degPerDir)) {
      d = 8;
    } else if (deg < (angle += degPerDir)) {
      d = 4;
    } else if (deg < (angle += degPerDir)) {
      d = 15;
    } else if (deg < (angle += degPerDir)) {
      d = 3;
    } else if (deg < (angle += degPerDir)) {
      d = 14;
    } else {
      d = 7;
    }

    animation.setDirection(d);
  }

  public String getCOF() {
    return cofName;
  }

  public void update() {
    String cofName = plrType + plrMode + weaponClass;
    if (DEBUG) Gdx.app.debug(TAG, "COF: " + this.cofName + " -> " + cofName);
    COFD2 chars_cof = COFD2.loadFromFile(Diablo.mpqs.resolve("data\\global\\chars_cof.d2"));
    cof = chars_cof.lookup(cofName);
    // FIXME: dispose/unload old animation layer
    //if (animation != null) animation.dispose();
    Animation oldAnim = animation;
    animation = new Animation.COFAnimation(cof);
    if (oldAnim != null) animation.setDirection(oldAnim.getDirection());

    String helm = "cap";

    Armor.Entry armor = Diablo.files.armor.get("ltp"); // light plate
    int rArm  = armor.rArm;
    int lArm  = armor.lArm;
    int Torso = armor.Torso;
    int Legs  = armor.Legs;
    int rSPad = armor.rSPad;
    int lSPad = armor.lSPad;
    System.out.printf("ArmType = %d, %d, %d, %d, %d, %d%n", rArm, lArm, Torso, Legs, rSPad, lSPad);

    String[] armorClasses = new String[15];
    armorClasses[HD] = helm;
    armorClasses[TR] = ARM_TYPE[Torso];
    armorClasses[LG] = ARM_TYPE[Legs];
    armorClasses[RA] = ARM_TYPE[rArm];
    armorClasses[LA] = ARM_TYPE[lArm];
    armorClasses[S1] = ARM_TYPE[lSPad];
    armorClasses[S2] = ARM_TYPE[rSPad];

    for (int i = 0; i < cof.getNumLayers(); i++) {
      COF.Layer layer = cof.getLayer(i);
      String component = LAYER[layer.component];
      String armorClass = armorClasses[layer.component];
      if (armorClass == null) {
        continue;
      }

      String weaponClass = layer.weaponClass;
      String path = CHARS + plrType + "\\" + component + "\\" + plrType + component + armorClass + plrMode + weaponClass + ".dcc";
      System.out.println(path);

      AssetDescriptor<DCC> descriptor = new AssetDescriptor<>(path, DCC.class);
      Diablo.assets.load(descriptor);
      Diablo.assets.finishLoadingAsset(descriptor);
      DCC dcc = Diablo.assets.get(descriptor);
      animation.setLayer(layer.component, dcc);
    }
  }

  // data\global\excel\PlrType.txt
  static class PlrType {
    static final String PT_AM = "am"; // amazon
    static final String PT_SO = "so"; // sorceress
    static final String PT_NE = "ne"; // necromancer
    static final String PT_PA = "pa"; // paladin
    static final String PT_BA = "ba"; // barbarian
    static final String PT_DZ = "dz"; // druid
    static final String PT_AI = "ai"; // assassin

    static String valueOf(CharClass charClass) {
      switch (charClass) {
        case AMAZON:      return PT_AM;
        case SORCERESS:   return PT_SO;
        case NECROMANCER: return PT_NE;
        case PALADIN:     return PT_PA;
        case BARBARIAN:   return PT_BA;
        case DRUID:       return PT_DZ;
        case ASSASSIN:    return PT_AI;
        default: throw new AssertionError("Unsupported class: " + charClass);
      }
    }
  }

  // data\global\excel\PlrMode.txt
  interface PlrMode {
    String PM_A1  = "a1";  // attack 1
    String PM_A2  = "a2";  // attack 2
    String PM_BL  = "bl";  // block
    String PM_DD  = "dd";  // body
    String PM_DTH = "dth"; // death
    String PM_GH  = "gh";  // get hit
    String PM_KK  = "kk";  // kick
    String PM_NU  = "nu";  // neutral (ready)
    String PM_RN  = "rn";  // run
    String PM_S1  = "s1";  // skill 1
    String PM_S2  = "s2";  // skill 2
    String PM_S3  = "s3";  // skill 3
    String PM_S4  = "s4";  // skill 4
    String PM_SC  = "sc";  // spell cast
    String PM_TH  = "th";  // throw
    String PM_TN  = "tn";  // town neutral
    String PM_TW  = "tw";  // town talk
    String PM_WL  = "wl";  // walk
  }

  // data\global\excel\WeaponClass.txt
  interface WeaponClass {
    String WC_HTH = "hth"; // hand-to-hand
    String WC_BOW = "bow"; // bow
    String WC_1HS = "1hs"; // 1 hand swing
    String WC_1HT = "1ht"; // 1 hand thrust
    String WC_STF = "stf"; // staff
    String WC_2HS = "2hs"; // 2 hand swing
    String WC_2HT = "2ht"; // 2 hand thrust
    String WC_XBW = "xbw"; // crossbow
    String WC_1JS = "1js"; // left jab right swing
    String WC_1JT = "1jt"; // left jab right thrust
    String WC_1SS = "1ss"; // left swing right swing
    String WC_1ST = "1st"; // left swing right thrust
    String WC_HT1 = "ht1"; // one hand-to-hand
    String WC_HT2 = "ht2"; // two hand-to-hand
  }

  // data\global\excel\ArmType.txt
  interface ArmType {
    String AT_LITE  = "lit";
    String AT_MED   = "med";
    String AT_HEAVY = "hvy";
  }

  interface HD {
    String HD_BHM = "bhm"; // bone helm
    String HD_CAP = "cap"; // cap
    String HD_CRN = "crn"; // crown
    String HD_FHL = "fhl"; // full helm
    String HD_GHM = "ghm"; // great helm
    String HD_HLM = "hlm"; // helm
    String HD_LIT = "lit"; // no helm
    String HD_MSK = "msk"; // mask
    String HD_SKP = "skp"; // skull cap

    String HD_DR1 = "dr1";
    String HD_DR3 = "dr3";
    String HD_DR4 = "dr4";
  }

  // data\global\excel\Composit.txt
  enum Composit {
    HD, // head
    TR, // torso
    LG, // legs
    RA, // right arm
    LA, // left arm
    RH, // right hand
    LH, // left hand
    SH, // shield
    S1, // special 1
    S2, // special 2
    S3, // special 3
    S4, // special 4
    S5, // special 5
    S6, // special 6
    S7, // special 7
    S8, // special 8
  }


  static final int HD = 0;  // head
  static final int TR = 1;  // torso
  static final int LG = 2;  // legs
  static final int RA = 3;  // right arm
  static final int LA = 4;  // left arm
  static final int RH = 5;  // right hand
  static final int LH = 6;  // left hand
  static final int SH = 7;  // shield
  static final int S1 = 8;  // special 1
  static final int S2 = 9;  // special 2
  static final int S3 = 10; // special 3
  static final int S4 = 11; // special 4
  static final int S5 = 12; // special 5
  static final int S6 = 13; // special 6
  static final int S7 = 14; // special 7
  static final int S8 = 15; // special 8

  String[] LAYER = {
      "HD", // head
      "TR", // torso
      "LG", // legs
      "RA", // right arm
      "LA", // left arm
      "RH", // right hand
      "LH", // left hand
      "SH", // shield
      "S1", // special 1
      "S2", // special 2
      "S3", // special 3
      "S4", // special 4
      "S5", // special 5
      "S6", // special 6
      "S7", // special 7
      "S8", // special 8
  };

  String[] ARM_TYPE = {
      "lit", "med", "hvy"
  };

}
