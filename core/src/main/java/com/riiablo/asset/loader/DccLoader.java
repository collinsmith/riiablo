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
import com.riiablo.asset.InvalidParams;
import com.riiablo.asset.param.DcParams;
import com.riiablo.file.Dcc;
import com.riiablo.file.DccDecoder;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.Pool;

public class DccLoader extends AssetLoader<Dcc> {
  private static final Logger log = LogManager.getLogger(DccLoader.class);

  private static final DcParams PARENT_DC = DcParams.of(-1);

  private final Pool<DccDecoder> decoders = new Pool<DccDecoder>(true, false, 4, 8) {
    @Override
    protected DccDecoder newInstance() {
      return new DccDecoder();
    }
  };

  @Override
  protected AssetDesc[] dependencies0(AssetDesc<Dcc> asset) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return super.dependencies0(asset);
    AssetDesc<Dcc> header = AssetDesc.of(asset, PARENT_DC);
    return new AssetDesc[] { header };
  }

  @Override
  protected void validate0(AssetDesc<Dcc> asset) {
    DcParams params = asset.params(DcParams.class);
    if (params.combineFrames == 1) throw new InvalidParams(asset,
        "Dcc does not support DcParams#combineFrames=1");
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Dcc> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      Dcc dcc = assets.getDepNow(AssetDesc.of(asset, PARENT_DC));
      int offset = dcc.dirOffset(params.direction);
      int nextOffset = dcc.dirOffset(params.direction + 1);
      return adapter.buffer(executor, handle, offset, nextOffset - offset);
    } else {
      return adapter.stream(executor, handle, adapter.defaultBufferSize(handle));
    }
  }

  @Override
  protected <F extends FileHandle> Dcc loadAsync0(
      AssetManager assets,
      AssetDesc<Dcc> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      boolean released = ReferenceCountUtil.release(handle); // dcc already owns a reference
      assert !released : handle + " was released, parent dc did not retain it";
      Dcc dcc = assets.getDepNow(AssetDesc.of(asset, PARENT_DC));
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
      dcc.read(buffer, params.direction);
      DccDecoder decoder = decoders.obtain();
      try {
        log.trace("decoding {}", asset);
        decoder.decode(dcc, params.direction);
      } finally {
        decoders.release(decoder);
      }
      return dcc;
    } else {
      assert data instanceof InputStream;
      InputStream stream = (InputStream) data;
      try {
        return Dcc.read(handle, stream);
      } finally {
        IOUtils.closeQuietly(stream);
      }
    }
  }

  @Override
  protected Dcc loadSync0(AssetManager assets, AssetDesc<Dcc> asset, Dcc dcc) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, dcc: {})", assets, asset, dcc);
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return dcc;
    dcc.uploadTextures(params.direction, params.combineFrames);
    return dcc;
  }
}
