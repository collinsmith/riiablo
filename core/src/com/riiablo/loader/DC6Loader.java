package com.riiablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.DC6;

public class DC6Loader extends AsynchronousAssetLoader<DC6, DC6Loader.DC6Parameters> {
  DC6 dc6;

  public DC6Loader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, DC6Parameters params) {
    dc6 = DC6.loadFromFile(file);
    if (params != null) {
      int preload = params.preload;
      if (preload == DC6Parameters.PRELOAD_ALL) {
        dc6.preloadDirections(params.combineFrames);
      } else if (preload > 0) {
        for (int d = 0; d < dc6.getNumDirections(); d++) {
          if ((preload & (1 << d)) != 0) dc6.preloadDirection(d);
        }
      }
    } else {
      dc6.preloadDirections(false);
    }
  }

  @Override
  public DC6 loadSync(AssetManager assets, String fileName, FileHandle file, DC6Parameters params) {
    DC6 dc6 = this.dc6;
    if (dc6 == null) {
      dc6 = DC6.loadFromFile(file);
    } else {
      this.dc6 = null;
    }

    if (params != null) {
      int preload = params.preload;
      if (preload == DC6Parameters.PRELOAD_ALL) {
        dc6.loadDirections(params.combineFrames);
      } else if (preload > 0) {
        for (int d = 0; d < dc6.getNumDirections(); d++) {
          if ((preload & (1 << d)) != 0) dc6.loadDirection(d);
        }
      }
    } else {
      dc6.loadDirections(false);
    }

    return dc6;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, DC6Parameters params) {
    return null;
  }

  public static class DC6Parameters extends AssetLoaderParameters<DC6> {
    public static final int PRELOAD_ALL = -1;

    public static final DC6Parameters COMBINE = new DC6Parameters(PRELOAD_ALL).combineFrames();

    public int preload;
    public boolean combineFrames;
    public DC6Parameters() {
      this(PRELOAD_ALL);
    }

    public DC6Parameters(int preload) {
      this.preload = preload;
      this.combineFrames = false;
    }

    public DC6Parameters combineFrames() {
      combineFrames = true;
      return this;
    }
  }
}
