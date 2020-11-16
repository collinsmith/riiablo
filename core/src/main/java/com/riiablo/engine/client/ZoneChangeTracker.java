package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.ZoneAware;
import com.riiablo.engine.server.event.ZoneChangeEvent;
import com.riiablo.map.Map;
import com.riiablo.map.RenderSystem;

import net.mostlyoriginal.api.event.common.EventSystem;

@All({ZoneAware.class, Position.class, MapWrapper.class})
public class ZoneChangeTracker extends IteratingSystem {
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<MapWrapper> mMapWrapper;

  protected RenderSystem renderer;
  protected EventSystem events;

  @Wire(name = "stage")
  protected Stage stage;

  @Wire(name = "map")
  protected Map map;

  @Override
  protected void process(int entityId) {
    Vector2 position = mPosition.get(entityId).position;
    MapWrapper mapWrapper = mMapWrapper.get(entityId);
    Map.Zone zone = mapWrapper.map.getZone(position);
    if (zone != mapWrapper.zone) {
      mapWrapper.zone = zone;
      events.dispatch(ZoneChangeEvent.obtain(entityId, zone));
    }
  }
}
