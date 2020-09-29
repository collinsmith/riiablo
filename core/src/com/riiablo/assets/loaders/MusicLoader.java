package com.riiablo.assets.loaders;

import io.netty.util.AsciiString;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.assets.Asset;
import com.riiablo.assets.AssetManager;
import com.riiablo.assets.AsyncAssetLoader;
import com.riiablo.assets.DelimiterPathTransformer;
import com.riiablo.assets.FileHandleResolver;

public class MusicLoader implements AsyncAssetLoader<Music, FileHandle> {
  @Override
  public FileHandleResolver resolver() {
    return new FileHandleResolver(DelimiterPathTransformer.INSTANCE) {
      @Override
      protected FileHandle resolveTransformed(AsciiString path) {
        throw new UnsupportedOperationException();
      }
    };
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

  @Override
  public Array<Asset> getDependencies(Asset<Music> asset) {
    throw new UnsupportedOperationException();
  }
}
