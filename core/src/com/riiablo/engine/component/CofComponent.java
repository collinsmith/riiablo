package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.engine.Dirty;

import java.util.Arrays;

public class CofComponent implements Component, Pool.Poolable {
  private static final String TAG = "CofComponent";

  public static final int MODE_NULL = -1;

  public static final String[] WCLASS = {
      "", "HTH", "BOW", "1HS", "1HT", "STF", "2HS", "2HT", "XBW", "1JS", "1JT", "1SS", "1ST", "HT1", "HT2"
  };

  public static final int WEAPON_NIL =  0;
  public static final int WEAPON_HTH =  1;
  public static final int WEAPON_BOW =  2;
  public static final int WEAPON_1HS =  3;
  public static final int WEAPON_1HT =  4;
  public static final int WEAPON_STF =  5;
  public static final int WEAPON_2HS =  6;
  public static final int WEAPON_2HT =  7;
  public static final int WEAPON_XBW =  8;
  public static final int WEAPON_1JS =  9;
  public static final int WEAPON_1JT = 10;
  public static final int WEAPON_1SS = 11;
  public static final int WEAPON_1ST = 12;
  public static final int WEAPON_HT1 = 13;
  public static final int WEAPON_HT2 = 14;

  public static final int COMPONENT_NULL = -1;
  public static final int COMPONENT_NIL  =  0;
  public static final int COMPONENT_LIT  =  1;
  public static final int[] DEFAULT_COMPONENT;
  static {
    DEFAULT_COMPONENT = new int[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_COMPONENT, COMPONENT_NIL);
  }

  public static final int TRANSFORM_NULL = -1;
  public static final int[] DEFAULT_TRANSFORM;
  static {
    DEFAULT_TRANSFORM = new int[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_TRANSFORM, TRANSFORM_NULL);
  }

  public String token;
  public int    mode;
  public int    wclass;
  public COF    cof;
  public int    dirty;
  public int    load;

  public final int[] component;
  public final int[] transform;

  public CofComponent() {
    component = new int[COF.Component.NUM_COMPONENTS];
    transform = new int[COF.Component.NUM_COMPONENTS];
    reset();
  }

  @SuppressWarnings("unchecked")
  public final AssetDescriptor<? extends DC>[] layer = (AssetDescriptor<DC>[]) new AssetDescriptor[COF.Component.NUM_COMPONENTS];

  @Override
  public void reset() {
    token  = null;
    mode   = MODE_NULL;
    wclass = WEAPON_NIL;
    cof    = null;
    dirty  = Dirty.NONE;
    load   = Dirty.NONE;
    System.arraycopy(DEFAULT_COMPONENT, 0, component, 0, COF.Component.NUM_COMPONENTS);
    System.arraycopy(DEFAULT_TRANSFORM, 0, transform, 0, COF.Component.NUM_COMPONENTS);
    Arrays.fill(layer, null);
  }
}
