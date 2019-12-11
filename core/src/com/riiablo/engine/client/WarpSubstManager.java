package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.riiablo.engine.client.component.Hovered;
import com.riiablo.engine.server.component.Warp;
import com.riiablo.map.Map;

@All({Warp.class, Hovered.class})
public class WarpSubstManager extends IteratingSystem {
  protected ComponentMapper<Warp> mWarp;

  @Wire(name = "map")
  protected Map map;

  @Override
  protected void begin() {
    map.clearWarpSubsts();
  }

  @Override
  protected void process(int entityId) {
    map.addWarpSubsts(mWarp.get(entityId).substs);
  }
}
