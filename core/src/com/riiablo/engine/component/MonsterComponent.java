package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;

public class MonsterComponent implements Component, Pool.Poolable {
  public MonStats.Entry  monstats;
  public MonStats2.Entry monstats2;

  @Override
  public void reset() {
    monstats = null;
    monstats2 = null;
  }
}
