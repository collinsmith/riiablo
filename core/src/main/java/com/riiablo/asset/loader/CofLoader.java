package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.file.Cof;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class CofLoader extends AssetLoader<Cof> {
  private static final Logger log = LogManager.getLogger(CofLoader.class);

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Cof> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    return adapter.buffer(executor, handle, 0, (int) handle.length());
  }

  @Override
  protected <F extends FileHandle> Cof loadAsync0(
      AssetManager assets,
      AssetDesc<Cof> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    assert data instanceof ByteBuf;
    ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
    try {
      return Cof.read(buffer);
    } finally {
      ReferenceCountUtil.release(handle);
    }
  }

  @Override
  protected Cof loadSync0(AssetManager assets, AssetDesc<Cof> asset, Cof cof) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, cof: {})", assets, asset, cof);
    return super.loadSync0(assets, asset, cof);
  }
}
