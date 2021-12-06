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
import com.riiablo.file.Palette;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class PaletteLoader extends AssetLoader<Palette> {
  private static final Logger log = LogManager.getLogger(PaletteLoader.class);

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Palette> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, assets: {}, asset: {}, handle: {}, adapter: {})", executor, assets, asset, handle, adapter);
    return adapter.buffer(executor, handle, 0, (int) handle.length());
  }

  @Override
  protected <F extends FileHandle> Palette loadAsync0(AssetManager assets, AssetDesc<Palette> asset, F handle, Object data) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    assert data instanceof ByteBuf;
    ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
    try {
      return Palette.read(buffer);
    } finally {
      ReferenceCountUtil.release(handle);
    }
  }

  @Override
  protected Palette loadSync0(AssetManager assets, AssetDesc<Palette> asset, Palette palette) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, object: {})", assets, asset, palette);
    return palette;
  }
}
