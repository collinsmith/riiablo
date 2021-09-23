package com.riiablo.asset.loader;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.FileHandleResolver;

public class MusicLoader extends AssetLoader<Music> {
  public MusicLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Music> asset,
      F handle,
      Adapter<F> adapter
  ) {
    return super.ioAsync(executor, assets, asset, handle, adapter);
  }

  @Override
  protected <F extends FileHandle> Music loadAsync(
      AssetManager assets,
      AssetDesc<Music> asset,
      F handle,
      Object data
  ) {
    return Gdx.audio.newMusic(handle);
  }

  @Override
  protected Music loadSync(
      AssetManager assets,
      AssetDesc<Music> asset,
      Music music
  ) {
    return music;
  }
}
