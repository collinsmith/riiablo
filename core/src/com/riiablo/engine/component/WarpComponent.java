package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;

public class WarpComponent implements Component, Pool.Poolable {
  public int           index;
  public LvlWarp.Entry warp;
  public Levels.Entry  dstLevel;

  public final IntIntMap substs = new IntIntMap();

  @Override
  public void reset() {
    index = -1;
    warp = null;
    dstLevel = null;
    substs.clear();
  }
}
