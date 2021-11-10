package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

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
  public Array<AssetDesc> dependencies(AssetDesc<Palette> asset) {
    return super.dependencies(asset);
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Palette> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync(executor: {}, assets: {}, asset: {}, handle: {}, adapter: {})", executor, assets, asset, handle, adapter);
    return adapter.buffer(executor, handle, 0, (int) handle.length());
  }

  @Override
  protected <F extends FileHandle> Palette loadAsync(AssetManager assets, AssetDesc<Palette> asset, F handle, Object data) {
    log.traceEntry("loadAsync(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    assert data instanceof ByteBuf;
    ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
    try {
      return Palette.read(buffer);
    } finally {
      ReferenceCountUtil.release(handle);
    }
  }

  @Override
  protected Palette loadSync(AssetManager assets, AssetDesc<Palette> asset, Palette palette) {
    log.traceEntry("loadSync(assets: {}, asset: {}, object: {})", assets, asset, palette);
    return palette;
  }
}
