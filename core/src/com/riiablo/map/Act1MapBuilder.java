package com.riiablo.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlPrest;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.map.Map.Preset;
import com.riiablo.map.Map.Zone;

public enum Act1MapBuilder implements MapBuilder {
  INSTANCE;

  private static final String TAG = "Act1MapBuilder";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_BUILD = DEBUG && true;

  @Override
  public void generate(Map map, int seed, int diff) {
    int def = Map.ACT_DEF[0];
    LvlPrest.Entry preset = Riiablo.files.LvlPrest.get(def);
    Levels.Entry   level  = Riiablo.files.Levels.get(preset.LevelId);
    if (DEBUG_BUILD) Gdx.app.debug(TAG, level.LevelName);

    int fileId[] = new int[6];
    int numFiles = Preset.getPresets(preset, fileId);
    int select = MathUtils.random(numFiles - 1);
    String fileName = preset.File[select];
    if (DEBUG_BUILD) Gdx.app.debug(TAG, "Select " + fileName);

    Zone zone = map.addZone(level, preset, select);
    zone.town = true;
    Zone prev = zone;

    level = Riiablo.files.Levels.get(2);
    zone = map.addZone(level, 8, 8);
    zone.setPosition(prev.width - zone.width, -zone.height);
    if (DEBUG_BUILD) Gdx.app.debug(TAG, "Moved " + zone.level.LevelName + " " + zone);

    Preset SB   = Preset.of(Riiablo.files.LvlPrest.get(4),  0);
    Preset EB   = Preset.of(Riiablo.files.LvlPrest.get(5),  0);
    Preset NB   = Preset.of(Riiablo.files.LvlPrest.get(6),  0);
    Preset WB   = Preset.of(Riiablo.files.LvlPrest.get(7),  0);
    Preset NWB  = Preset.of(Riiablo.files.LvlPrest.get(9),  0);
    Preset LRC  = Preset.of(Riiablo.files.LvlPrest.get(27), 0);
    Preset UR   = Preset.of(Riiablo.files.LvlPrest.get(26), 3);
    Preset URNB = Preset.of(Riiablo.files.LvlPrest.get(26), 1);
    Preset SWB  = Preset.of(Riiablo.files.LvlPrest.get(8),  0);
    Preset LB   = Preset.of(Riiablo.files.LvlPrest.get(12), 0);

    for (int y = 0; y < zone.gridsY; y++) {
      zone.presets[0][y] = EB;
      zone.presets[zone.gridsX - 2][y] = UR;
      zone.presets[zone.gridsX - 1][y] = LRC;
    }
    for (int x = 1; x < zone.gridsX - 1; x++) {
      zone.presets[x][0] = NB;
    }
    zone.presets[0][0] = NWB;
    zone.presets[zone.gridsX - 2][0] = URNB;
    zone.presets[zone.gridsX - 1][0] = LRC;
    zone.presets[0][zone.gridsY - 1] = SWB;
    zone.presets[1][zone.gridsY - 1] = SB;
    zone.presets[2][zone.gridsY - 1] = SB;
    zone.presets[3][zone.gridsY - 1] = LB;

    zone.presets[6][zone.gridsY - 2] = Preset.of(Riiablo.files.LvlPrest.get(47), 1);

    // ID_VIS_5_42
    zone.presets[5][zone.gridsY - 2] = Preset.of(Riiablo.files.LvlPrest.get(52), 0);
    zone.setWarp(Map.ID.VIS_5_42, Map.ID.VIS_0_03);
    zone.generator = new Zone.Generator() {
      final float SPAWN_MULT = 2f;
      MonStats.Entry[] monsters;

      @Override
      public void init(Zone zone) {
        int prob = 0;
        int numMon = zone.level.NumMon;
        MonStats.Entry[] monstats = new MonStats.Entry[numMon];
        for (int i = 0; i < numMon; i++) {
          String mon = zone.level.mon[i];
          monstats[i] = Riiablo.files.monstats.get(mon);
          prob += monstats[i].Rarity;
        }

        monsters = new MonStats.Entry[prob];
        prob = 0;
        for (MonStats.Entry entry : monstats) {
          for (int i = 0; i < entry.Rarity; i++) {
            monsters[prob++] = entry;
          }
        }
      }

      @Override
      public void generate(Zone zone, DT1s dt1s, int tx, int ty) {
        final int startY = ty;
        for (int x = 0; x < zone.gridSizeX; x++, tx++, ty = startY) {
          for (int y = 0; y < zone.gridSizeY; y++, ty++) {
            // TODO: Zone.index() can be replaced with incrementer
            zone.getLayer(Map.FLOOR_OFFSET)[Zone.index(zone.tilesX, tx, ty)] = dt1s.get(0);
            if (MathUtils.randomBoolean(SPAWN_MULT * zone.level.MonDen[zone.diff] / 100000f)) {
              int i = MathUtils.random(monsters.length - 1);
              MonStats.Entry monster = monsters[i];
              int count = monster.MinGrp == monster.MaxGrp
                  ? monster.MaxGrp
                  : MathUtils.random(monster.MinGrp, monster.MaxGrp);
              for (i = 0; i < count; i++) {
                int px = zone.getGlobalX(tx * DT1.Tile.SUBTILE_SIZE);
                int py = zone.getGlobalY(ty * DT1.Tile.SUBTILE_SIZE);
                Entity entity = Riiablo.engine.createMonster(zone.map, zone, monster, px, py);
                Riiablo.engine.addEntity(entity);
              }
            }
          }
        }
      }
    };

    level = Riiablo.files.Levels.get(8);
    zone = map.addZone(level, 24, 24);
    zone.setPosition(level.OffsetX, level.OffsetY);
    if (DEBUG_BUILD) Gdx.app.debug(TAG, "Moved " + zone.level.LevelName + " " + zone);
    zone.presets[0][1] = Preset.of(Riiablo.files.LvlPrest.get(84), 0);
    zone.presets[1][1] = Preset.of(Riiablo.files.LvlPrest.get(61), 1);
    zone.presets[1][0] = Preset.of(Riiablo.files.LvlPrest.get(97), 0);
    zone.setWarp(Map.ID.VIS_0_03, Map.ID.VIS_5_42);
  }
}
