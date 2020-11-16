package com.riiablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlPrest;
import com.riiablo.map.Map.Preset;
import com.riiablo.map.Map.Zone;

public enum Act5MapBuilder implements MapBuilder {
  INSTANCE;

  private static final String TAG = "Act5MapBuilder";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_BUILD = DEBUG && true;

  @Override
  public void generate(Map map, int seed, int diff) {
    int def = Map.ACT_DEF[4];
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
  }
}
