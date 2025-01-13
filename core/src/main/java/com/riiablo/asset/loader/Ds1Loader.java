package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map5.Ds1;
import com.riiablo.map5.Ds1Decoder;

public class Ds1Loader extends AssetLoader<Ds1> {
  private static final Logger log = LogManager.getLogger(Ds1Loader.class);

  @Override
  protected AssetDesc[] dependencies0(AssetDesc<Ds1> asset) {
    return super.dependencies0(asset);
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Ds1> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    return adapter.buffer(executor, handle, 0, (int) handle.length());
  }

  @Override
  protected <F extends FileHandle> Ds1 loadAsync0(
      AssetManager assets,
      AssetDesc<Ds1> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
    log.trace("decoding {}", asset);
    return Ds1Decoder.decode(handle, ByteInput.wrap(buffer));
  }

  @Override
  protected Ds1 loadSync0(AssetManager assets, AssetDesc<Ds1> asset, Ds1 ds1) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, ds1: {})", assets, asset, ds1);
    return ds1;
  }
}
