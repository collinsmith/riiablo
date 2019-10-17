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

  public static final String[] WCLASS = {
      "", "HTH", "BOW", "1HS", "1HT", "STF", "2HS", "2HT", "XBW", "1JS", "1JT", "1SS", "1ST", "HT1", "HT2"
  };

  public static final byte WEAPON_NIL =  0;
  public static final byte WEAPON_HTH =  1;
  public static final byte WEAPON_BOW =  2;
  public static final byte WEAPON_1HS =  3;
  public static final byte WEAPON_1HT =  4;
  public static final byte WEAPON_STF =  5;
  public static final byte WEAPON_2HS =  6;
  public static final byte WEAPON_2HT =  7;
  public static final byte WEAPON_XBW =  8;
  public static final byte WEAPON_1JS =  9;
  public static final byte WEAPON_1JT = 10;
  public static final byte WEAPON_1SS = 11;
  public static final byte WEAPON_1ST = 12;
  public static final byte WEAPON_HT1 = 13;
  public static final byte WEAPON_HT2 = 14;

  public static final byte COMPONENT_0xFF = -1;
  public static final byte COMPONENT_NIL  =  0;
  public static final byte COMPONENT_LIT  =  1;

  public String token  = null;
  public byte   mode   = 0;
  public byte   wclass = WEAPON_NIL;
  public COF    cof    = null;
  public int    dirty  = Dirty.NONE;
  public int    load   = Dirty.NONE;

  public final byte[] component = new byte[COF.Component.NUM_COMPONENTS];

  @SuppressWarnings("unchecked")
  public final AssetDescriptor<? extends DC>[] layer = (AssetDescriptor<DC>[]) new AssetDescriptor[COF.Component.NUM_COMPONENTS];

  @Override
  public void reset() {
    token  = null;
    mode   = 0;
    wclass = WEAPON_NIL;
    cof    = null;
    dirty  = Dirty.NONE;
    load   = Dirty.NONE;
    Arrays.fill(component, COMPONENT_NIL);
    Arrays.fill(layer, null);
  }
}
