package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Missiles;

public class MissileComponent implements Component, Pool.Poolable {
  public Missiles.Entry missile;
  public float range = 0;
  public final Vector2 start = new Vector2();
  public AssetDescriptor<DCC> missileDescriptor;

  @Override
  public void reset() {
    missile = null;
    range = 0;
    start.setZero();
    missileDescriptor = null;
  }
}
