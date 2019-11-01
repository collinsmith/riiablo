package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;

public class MonsterComponent implements Component, Pool.Poolable {
  public Map             map;
  public Map.Zone        zone;
  public DS1             ds1;
  public DS1.Object      object;
  public MonStats.Entry  monstats;
  public MonStats2.Entry monstats2;

  @Override
  public void reset() {
    map = null;
    zone = null;
    ds1 = null;
    object = null;
    monstats = null;
    monstats2 = null;
  }
}
