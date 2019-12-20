package com.riiablo.map;

import com.artemis.annotations.Wire;
import com.badlogic.gdx.utils.IntMap;
import com.riiablo.engine.EntityFactory;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public class MapManager extends PassiveSystem {

  @Wire(name = "map")
  protected Map map;

  @Wire(name = "factory")
  protected EntityFactory factory;

  public void createEntities() {
    for (Map.Zone zone : map.zones) {
      createWarps(zone);
      createEntities(zone);
    }
  }

  private void createWarps(Map.Zone zone) {
    IntMap<DS1.Cell> specials = zone.specials;
    for (IntMap.Entry<DS1.Cell> entry : specials.entries()) {
      DS1.Cell cell = entry.value;
      if (Map.ID.WARPS.contains(cell.id)) {
        int hash = entry.key;
        int x = zone.x + (Map.Zone.tileHashX(hash) * DT1.Tile.SUBTILE_SIZE);
        int y = zone.y + (Map.Zone.tileHashY(hash) * DT1.Tile.SUBTILE_SIZE);
        int id = factory.createWarp(map, zone, cell.id, x, y);
        zone.entities.add(id);
      }
    }
  }

  public void createEntities(Map.Zone zone) {
    for (int x = 0, gridX = 0, gridY = 0; x < zone.gridsX; x++, gridX += zone.gridSizeX, gridY = 0) {
      for (int y = 0; y < zone.gridsY; y++, gridY += zone.gridSizeY) {
        Map.Preset preset = zone.presets[x][y];
        if (preset == null) continue;
        createEntities(zone, preset, gridX, gridY);
      }
    }
  }

  private void createEntities(Map.Zone zone, Map.Preset preset, int gridX, int gridY) {
    final int x = zone.x + (gridX * DT1.Tile.SUBTILE_SIZE);
    final int y = zone.y + (gridY * DT1.Tile.SUBTILE_SIZE);
    DS1 ds1 = preset.ds1;
    for (int i = 0, size = ds1.numObjects; i < size; i++) {
      DS1.Object object = ds1.objects[i];
      int id = factory.createObject(map, zone, preset, object, x + object.x, y + object.y);
      zone.entities.add(id);
    }
  }
}
