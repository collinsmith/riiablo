package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.Engine;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.MovementModeComponent;
import com.riiablo.engine.component.PlayerComponent;
import com.riiablo.engine.component.ZoneUpdate;
import com.riiablo.map.Map;

public class ZoneUpdateSystem extends IteratingSystem {
  private final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);
  private final ComponentMapper<PlayerComponent> playerComponent = ComponentMapper.getFor(PlayerComponent.class);
  private final ComponentMapper<MovementModeComponent> movementModeComponent = ComponentMapper.getFor(MovementModeComponent.class);

  public ZoneUpdateSystem() {
    super(Family.all(ZoneUpdate.class, MapComponent.class, MovementModeComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    MapComponent mapComponent = this.mapComponent.get(entity);
    Map.Zone zone = mapComponent.zone;
    if (playerComponent.has(entity)) {
      MovementModeComponent movementModeComponent = this.movementModeComponent.get(entity);
      if (zone.isTown()) {
        movementModeComponent.NU = Engine.Player.MODE_TN;
        movementModeComponent.WL = Engine.Player.MODE_TW;
        movementModeComponent.RN = Engine.Player.MODE_RN;
      } else {
        movementModeComponent.NU = Engine.Player.MODE_NU;
        movementModeComponent.WL = Engine.Player.MODE_WL;
        movementModeComponent.RN = Engine.Player.MODE_RN;
      }
    }

    entity.remove(ZoneUpdate.class);
  }
}
