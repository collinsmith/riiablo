package gdx.diablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gdx.diablo.codec.DCC;

public class DCCLoader extends AsynchronousAssetLoader<DCC, DCCLoader.DCCParameters> {
  DCC dcc;

  public DCCLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, DCCParameters params) {
    dcc = DCC.loadFromFile(file);
    if (params != null) {
      int preload = params.preload;
      if (preload == DCCParameters.PRELOAD_ALL) {
        dcc.preloadDirections(params.combineFrames);
      } else if (preload > 0) {
        for (int d = 0; d < dcc.getNumDirections(); d++) {
          if ((preload & (1 << d)) != 0) dcc.preloadDirection(d);
        }
      }
    } else {
      dcc.preloadDirections(false);
    }
  }

  @Override
  public DCC loadSync(AssetManager assets, String fileName, FileHandle file, DCCParameters params) {
    DCC dcc = this.dcc;
    if (dcc == null) {
      dcc = DCC.loadFromFile(file);
    } else {
      this.dcc = null;
    }

    if (params != null) {
      int preload = params.preload;
      if (preload == DCCParameters.PRELOAD_ALL) {
        dcc.loadDirections(params.combineFrames);
      } else if (preload > 0) {
        for (int d = 0; d < dcc.getNumDirections(); d++) {
          if ((preload & (1 << d)) != 0) dcc.loadDirection(d);
        }
      }
    } else {
      dcc.loadDirections(false);
    }

    return dcc;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, DCCParameters params) {
    return null;
  }

  public static class DCCParameters extends AssetLoaderParameters<DCC> {
    public static final int PRELOAD_ALL = -1;

    public static final DCCParameters COMBINE = new DCCParameters(PRELOAD_ALL).combineFrames();

    public int preload;
    public boolean combineFrames;
    public DCCParameters() {
      this(PRELOAD_ALL);
    }

    public DCCParameters(int preload) {
      this.preload = preload;
      this.combineFrames = false;
    }

    public DCCParameters combineFrames() {
      combineFrames = true;
      return this;
    }
  }
}
