package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.component.HoveredComponent;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.map.Map;

public class WarpSystem extends IteratingSystem {
  private final ComponentMapper<WarpComponent> warpComponent = ComponentMapper.getFor(WarpComponent.class);

  private Map map;

  public WarpSystem(Map map) {
    super(Family.all(WarpComponent.class, HoveredComponent.class).get());
    this.map = map;
  }

  @Override
  public void update(float delta) {
    map.clearWarpSubsts();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    WarpComponent warpComponent = this.warpComponent.get(entity);
    map.addWarpSubsts(warpComponent.substs);
  }
}
