package com.riiablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.COF;

public class COFLoader extends AsynchronousAssetLoader<COF, COFLoader.COFLoaderParameters> {

  COF cof;

  public COFLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, COFLoaderParameters params) {
    cof = COF.loadFromFile(file);
  }

  @Override
  public COF loadSync(AssetManager assets, String fileName, FileHandle file, COFLoaderParameters params) {
    COF cof = this.cof;
    if (cof == null) {
      cof = COF.loadFromFile(file);
    } else {
      this.cof = null;
    }

    return cof;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, COFLoaderParameters params) {
    return null;
  }

  public static class COFLoaderParameters extends AssetLoaderParameters<COF> {}
}
