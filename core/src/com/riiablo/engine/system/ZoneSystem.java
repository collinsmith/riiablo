package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.Engine;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.ZoneAwareComponent;
import com.riiablo.engine.component.ZoneUpdate;
import com.riiablo.map.Map;

public class ZoneSystem extends IteratingSystem {
  private final ComponentMapper<ZoneUpdate> zoneUpdateComponent = ComponentMapper.getFor(ZoneUpdate.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);

  public ZoneSystem() {
    super(Family.all(ZoneAwareComponent.class, PositionComponent.class, MapComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    PositionComponent positionComponent = this.positionComponent.get(entity);
    MapComponent mapComponent = this.mapComponent.get(entity);
    Map.Zone zone = mapComponent.map.getZone(positionComponent.position);
    if (zone != mapComponent.zone) {
      mapComponent.zone = zone;
      Engine.getOrCreateComponent(entity, getEngine(), ZoneUpdate.class, zoneUpdateComponent);
    }
  }
}
