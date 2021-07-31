package com.riiablo.map2;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.codec.util.BBox;

public class Prefab extends BBox implements Poolable {
  static final Pool<Prefab> pool = Pools.get(Prefab.class);

  public String name;
  public int group;

  @Override
  public void reset() {
    super.reset();
    name = null;
    group = 0;
  }
}
