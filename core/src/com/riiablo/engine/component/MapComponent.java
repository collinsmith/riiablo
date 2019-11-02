package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.map.Map;

public class MapComponent implements Component, Pool.Poolable {
  public Map      map;
  public Map.Zone zone;

  @Override
  public void reset() {
    map = null;
    zone = null;
  }
}
