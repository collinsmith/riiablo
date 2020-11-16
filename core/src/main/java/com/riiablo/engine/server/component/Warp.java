package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;

@Transient
@PooledWeaver
public class Warp extends PooledComponent {
  public int           index;
  public LvlWarp.Entry warp;
  public Levels.Entry  dstLevel;

  public final IntIntMap substs = new IntIntMap();

  @Override
  protected void reset() {
    index = 0;
    warp = null;
    dstLevel = null;
    substs.clear();
  }

  public Warp set(int index, LvlWarp.Entry warp, Levels.Entry dstLevel) {
    this.index = index;
    this.warp = warp;
    this.dstLevel = dstLevel;
    return this;
  }
}
