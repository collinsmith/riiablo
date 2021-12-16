package com.riiablo.asset;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import com.badlogic.gdx.files.FileHandle;

import static com.riiablo.asset.AssetDesc.EMPTY_ASSET_DESC_ARRAY;

public abstract class AssetLoader<T> {
  public final AssetDesc[] dependencies(
      Promise<T> promise,
      AssetDesc<T> asset
  ) {
    try {
      return dependencies0(asset);
    } catch (Throwable t) {
      promise.setFailure(t);
      throw t;
    }
  }

  public final void validate(
      Promise<T> promise,
      AssetDesc<T> asset
  ) {
    try {
      validate0(asset);
    } catch (Throwable t) {
      promise.setFailure(t);
      throw t;
    }
  }

  public final <F extends FileHandle> Future<?> ioAsync(
      Promise<T> promise,
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Adapter<F> adapter
  ) {
    try {
      return ioAsync0(executor, assets, asset, handle, adapter);
    } catch (Throwable t) {
      promise.setFailure(t);
      throw t;
    }
  }

  public final <F extends FileHandle> T loadAsync(
      Promise<T> promise,
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Object data
  ) {
    try {
      return loadAsync0(assets, asset, handle, data);
    } catch (Throwable t) {
      promise.setFailure(t);
      throw t;
    }
  }

  public final T loadSync(
      Promise<T> promise,
      AssetManager assets,
      AssetDesc<T> asset,
      T object
  ) {
    try {
      return loadSync0(assets, asset, object);
    } catch (Throwable t) {
      promise.setFailure(t);
      throw t;
    }
  }

  protected AssetDesc[] dependencies0(
      AssetDesc<T> asset
  ) {
    return EMPTY_ASSET_DESC_ARRAY;
  }

  protected void validate0(
      AssetDesc<T> asset
  ) {
  }

  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Adapter<F> adapter
  ) {
    return executor.newSucceededFuture(null);
  }

  protected <F extends FileHandle> T loadAsync0(
      AssetManager assets,
      AssetDesc<T> asset,
      F handle,
      Object data
  ) {
    return null;
  }

  protected T loadSync0(
      AssetManager assets,
      AssetDesc<T> asset,
      T object
  ) {
    return object;
  }
}
