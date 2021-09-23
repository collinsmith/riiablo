package com.riiablo.asset;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public abstract class AssetLoader<T> {
  final FileHandleResolver resolver;

  protected AssetLoader(FileHandleResolver resolver) {
    this.resolver = resolver;
  }

  public final FileHandleResolver resolver() {
    return resolver;
  }

  public Array<AssetDesc> dependencies(AssetDesc<T> asset) {
    return EmptyArray.empty();
  }

  protected <F extends FileHandle> Future<?> ioAsync(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Adapter<F> adapter
  ) {
    return executor.newSucceededFuture(null);
  }

  protected <F extends FileHandle> T loadAsync(
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Object data
  ) {
    return null;
  }

  protected T loadSync(
      AssetManager assets,
      AssetDesc<T> asset,
      T object
  ) {
    return object;
  }
}
