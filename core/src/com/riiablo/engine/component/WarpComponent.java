package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;

public class WarpComponent implements Component, Pool.Poolable {
  public int           index;
  public LvlWarp.Entry warp;
  public Levels.Entry  dstLevel;

  @Override
  public void reset() {
    index = -1;
    warp = null;
    dstLevel = null;
  }
}
