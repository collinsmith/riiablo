package com.riiablo.assets.loaders;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.riiablo.assets.Asset;
import com.riiablo.assets.AssetManager;
import com.riiablo.assets.AsyncAssetLoader;
import com.riiablo.assets.FileHandleResolver;

public class MusicLoader extends AsyncAssetLoader<Music, FileHandle> {
  MusicLoader(FileHandleResolver resolver) {
    super(resolver, FileHandle.class);
  }

  @Override
  public void loadAsync(AssetManager assets, Asset<Music> asset, FileHandle data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unloadAsync(AssetManager assets, Asset<Music> asset, FileHandle data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Music loadSync(AssetManager assets, Asset<Music> asset, FileHandle data) {
    throw new UnsupportedOperationException();
  }
}
