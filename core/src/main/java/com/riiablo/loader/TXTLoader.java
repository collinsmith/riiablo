package com.riiablo.loader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.codec.TXT;

public class TXTLoader extends AsynchronousAssetLoader<TXT, TXTLoader.TXTLoaderParameters> {

  TXT txt;

  public TXTLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager assets, String fileName, FileHandle file, TXTLoaderParameters params) {
    txt = TXT.loadFromFile(file);
  }

  @Override
  public TXT loadSync(AssetManager assets, String fileName, FileHandle file, TXTLoaderParameters params) {
    TXT txt = this.txt;
    this.txt = null;
    return txt != null ? txt : TXT.loadFromFile(file);
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TXTLoaderParameters params) {
    return null;
  }

  public static class TXTLoaderParameters extends AssetLoaderParameters<TXT> {
    public final String  id;
    public final boolean asInt;
    public TXTLoaderParameters(String id) {
      this(id, true);
    }
    public TXTLoaderParameters(String id, boolean asInt) {
      this.id = id;
      this.asInt = asInt;
    }
  }

}
