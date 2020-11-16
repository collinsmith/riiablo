package com.riiablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.Index;

public class IndexLoader extends AsynchronousAssetLoader<Index, IndexLoader.IndexParameter> {

  Index index;

  public IndexLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, IndexParameter params) {
    index = Index.loadFromFile(file);
  }

  @Override
  public Index loadSync(AssetManager assets, String fileName, FileHandle file, IndexParameter params) {
    Index index = this.index;
    if (index == null) {
      index = Index.loadFromFile(file);
    } else {
      this.index = null;
    }

    return index;
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, IndexParameter params) {
    return null;
  }

  public static class IndexParameter extends AssetLoaderParameters<Index> {}

}
