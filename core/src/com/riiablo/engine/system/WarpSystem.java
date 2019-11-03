package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.map.Map;

public class WarpSystem extends IteratingSystem {
  private final ComponentMapper<WarpComponent> warpComponent = ComponentMapper.getFor(WarpComponent.class);

  private Map map;

  public WarpSystem() {
    super(Family.all(WarpComponent.class, MapComponent.class).get());
  }

  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    if (this.map != map) {
      this.map = map;
    }
  }

  @Override
  public void update(float delta) {
    map.clearWarpSubsts();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    WarpComponent warpComponent = this.warpComponent.get(entity);
    if ((entity.flags & Flags.SELECTED) == Flags.SELECTED) {
      map.addWarpSubsts(warpComponent.substs);
    }
  }
}
