package com.riiablo.map;

/*public class MapLoader implements AsyncTask<Map> {
  int seed;
  int act;
  int diff;

  public MapLoader(int seed, int act, int diff) {
    this.seed = seed;
    this.act  = act;
    this.diff = diff;
  }

  @Override
  public Map call() {
    return Map.build(seed, act, diff);
  }
}*/

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.LvlPrest;
import com.riiablo.codec.excel.LvlTypes;

public class MapLoader extends AsynchronousAssetLoader<Map, MapLoader.MapParameters> {
  Map map;

  public MapLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, MapLoader.MapParameters params) {
    map = Map.build(params);
  }

  @Override
  public Map loadSync(AssetManager assets, String fileName, FileHandle file, MapLoader.MapParameters params) {
    Map map = this.map;
    if (map == null) {
      map = Map.build(params);
    } else {
      this.map = null;
    }

    map.buildDT1s();
    return map;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MapLoader.MapParameters params) {
    Array<AssetDescriptor> dependencies = new Array<>();
    for (int def = Map.ACT_DEF[params.act]; def < /*53*/Map.ACT_DEF[params.act + 1]; def++) {
      LvlPrest.Entry preset = Riiablo.files.LvlPrest.get(def);
      for (int i = 0; i < preset.File.length; i++) {
        if (preset.File[i].charAt(0) != '0') {
          dependencies.add(new AssetDescriptor<>(Map.TILES_PATH + preset.File[i], DS1.class));
        }
      }
    }

    for (LvlTypes.Entry type : Riiablo.files.LvlTypes) {
      if ((type.Act - 1) == params.act && type.Id <= 3) {
        for (int i = 0; type.File[i].charAt(0) != '0'; i++) {
          dependencies.add(new AssetDescriptor<>(Map.TILES_PATH + type.File[i], DT1.class));
        }
      }
    }

    return dependencies;
  }

  public static class MapParameters extends AssetLoaderParameters<Map> {
    public int seed;
    public int act;
    public int diff;

    public static MapParameters of(int seed, int act, int diff) {
      MapParameters params = new MapParameters();
      params.seed = seed;
      params.act  = act;
      params.diff = diff;
      return params;
    }
  }
}
