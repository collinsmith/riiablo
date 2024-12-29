package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.param.Dt1Params;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map5.Dt1;
import com.riiablo.map5.Dt1Decoder;

public class Dt1Loader extends AssetLoader<Dt1> {
  private static final Logger log = LogManager.getLogger(Dt1Loader.class);

  private static final Dt1Params PARENT_DT1 = Dt1Params.library();

  @Override
  protected AssetDesc[] dependencies0(AssetDesc<Dt1> asset) {
    Dt1Params params = asset.params(Dt1Params.class);
    if (params.tileId < 0) return super.dependencies0(asset);
    AssetDesc<Dt1> header = AssetDesc.of(asset, PARENT_DT1);
    return new AssetDesc[] { header };
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Dt1> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    Dt1Params params = asset.params(Dt1Params.class);
    if (params.tileId >= 0) {
      Dt1 dt1 = assets.getDepNow(AssetDesc.of(asset, PARENT_DT1));
      int offset = dt1.blocksOffset(params.tileId);
      int length = dt1.blocksLength(params.tileId);
      return adapter.buffer(executor, handle, offset, length);
    } else {
      return adapter.stream(executor, handle, adapter.defaultBufferSize(handle));
    }
  }

  @Override
  protected <F extends FileHandle> Dt1 loadAsync0(
      AssetManager assets,
      AssetDesc<Dt1> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    Dt1Params params = asset.params(Dt1Params.class);
    if (params.tileId >= 0) {
      boolean released = ReferenceCountUtil.release(handle); // dt1 already owns a reference
      assert !released : handle + " was released, parent dt1 did not retain it";
      Dt1 dt1 = assets.getDepNow(AssetDesc.of(asset, PARENT_DT1));
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
      dt1.retain(); // increment refCnt for each tile read
      log.trace("decoding {}", asset);
      Dt1Decoder.decodeTile(dt1, ByteInput.wrap(buffer), params.tileId);
      return dt1;
    } else {
      assert data instanceof InputStream;
      InputStream stream = (InputStream) data;
      try {
        return Dt1Decoder.decode(handle, stream);
      } finally {
        IOUtils.closeQuietly(stream);
      }
    }
  }

  @Override
  protected Dt1 loadSync0(AssetManager assets, AssetDesc<Dt1> asset, Dt1 dt1) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, dt1: {})", assets, asset, dt1);
    Dt1Params params = asset.params(Dt1Params.class);
    if (params.tileId < 0) return dt1;
    dt1.uploadTexture(params.tileId);
    return dt1;
  }
}
