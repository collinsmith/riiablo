package gdx.diablo.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.apache.commons.io.FilenameUtils;

import gdx.diablo.Diablo;
import gdx.diablo.codec.DS1;
import gdx.diablo.codec.DT1;
import gdx.diablo.codec.Map;

public class MapLoader extends AsynchronousAssetLoader<Map, MapLoader.MapLoaderParameters> {
  private static final String TAG = "MapLoader";
  private static final boolean DEBUG = true;

  Map    map;

  String ds1FileName;
  DS1    ds1;

  int    numDt1Files;
  String dt1FileName[] = new String[Integer.SIZE];
  DT1    dt1[];

  public MapLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, MapLoaderParameters params) {
    ds1 = assets.get(ds1FileName, DS1.class);
    dt1 = new DT1[numDt1Files];
    for (int i = 0; i < numDt1Files; i++) {
      dt1[i] = assets.get(dt1FileName[i], DT1.class);
    }

    //map = new Map(ds1, dt1);
  }

  @Override
  public Map loadSync(AssetManager assets, String fileName, FileHandle file, MapLoaderParameters params) {
    /*Map map = this.map;
    this.map = null;
    if (map == null) {
      ds1 = assets.get(ds1FileName, DS1.class);
      dt1 = new DT1[numDt1Files];
      for (int i = 0; i < numDt1Files; i++) {
        dt1[i] = assets.get(dt1FileName[i], DT1.class);
      }

      map = new Map(ds1, dt1);
    }

    return map;*/
    return new Map(ds1, dt1);
  }

  // TODO: Passing TXT with params and also with dependencies? Circular much?

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MapLoaderParameters params) {
    assert FilenameUtils.isExtension(fileName, "map") : "Invalid extension: " + FilenameUtils.getExtension(fileName);
    ds1FileName = fileName.substring(0, FilenameUtils.indexOfExtension(fileName));

    Array<AssetDescriptor> deps = new Array<>(32);
    deps.add(new AssetDescriptor<>(ds1FileName, DS1.class));

    final int Dt1Mask = Diablo.files.LvlPrest.get(params.def).Dt1Mask;
    final boolean[] exists = new boolean[Integer.SIZE];
    for (int i = 0; i < exists.length; i++) {
      exists[i] = (Dt1Mask & (1 << i)) != 0;
    }

    final int type = params.type;
    for (int i = 0; i < exists.length; i++) {
      String text = Diablo.files.LvlTypes.get(type).File[i];
      if ((Dt1Mask & (1 << i)) != 0) {
        text = "data/global/tiles/" + text;
        dt1FileName[numDt1Files++] = text;
        deps.add(new AssetDescriptor<>(text, DT1.class));
      } else if (text.charAt(0) != '0') {
        Gdx.app.debug(TAG, "skipping " + text);
      }
    }

    if (DEBUG) {
      for (int i = 0; i < deps.size; i++) {
        Gdx.app.debug(TAG, "dep[" + i + "] = " + deps.get(i));
      }
    }

    return deps;
  }

  public static class MapLoaderParameters extends AssetLoaderParameters<Map> {
    public final int def; // from LvlPrest
    public final int type; // from LvlTypes

    public MapLoaderParameters(int def, int type) {
      this.def = def;
      this.type = type;
    }
  }

}
