package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.EmptyArray;
import com.riiablo.asset.param.DcParams;
import com.riiablo.file.Dc6;
import com.riiablo.file.Dc6Decoder;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.util.Pool;

public class Dc6Loader extends AssetLoader<Dc6> {
  private static final Logger log = LogManager.getLogger(Dc6Loader.class);

  private static final DcParams PARENT_DC = DcParams.of(-1);

  private final Pool<Dc6Decoder> decoders = new Pool<Dc6Decoder>(true, false, 4, 8) {
    @Override
    protected Dc6Decoder newInstance() {
      return new Dc6Decoder();
    }
  };

  @Override
  public Array<AssetDesc> dependencies0(AssetDesc<Dc6> asset) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return EmptyArray.empty();
    AssetDesc<Dc6> header = AssetDesc.of(asset, PARENT_DC);
    final Array<AssetDesc> dependencies = Array.of(false, 1, AssetDesc.class);
    dependencies.add(header);
    return dependencies;
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Dc6> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      Dc6 dc6 = assets.getDepNow(AssetDesc.of(asset, PARENT_DC));
      int offset = dc6.dirOffset(params.direction);
      int nextOffset = dc6.dirOffset(params.direction + 1);
      return adapter.buffer(executor, handle, offset, nextOffset - offset);
    } else {
      return adapter.stream(executor, handle, adapter.defaultBufferSize(handle));
    }
  }

  @Override
  protected <F extends FileHandle> Dc6 loadAsync0(
      AssetManager assets,
      AssetDesc<Dc6> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      boolean released = ReferenceCountUtil.release(handle); // dcc already owns a reference
      assert !released : handle + " was released, parent dc did not retain it";
      Dc6 dc6 = assets.getDepNow(AssetDesc.of(asset, PARENT_DC));
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
      dc6.read(buffer, params.direction);
      Dc6Decoder decoder = decoders.obtain();
      try {
        log.trace("decoding {}", asset);
        decoder.decode(dc6, params.direction);
      } finally {
        decoders.release(decoder);
      }
      return dc6;
    } else {
      assert data instanceof InputStream;
      InputStream stream = (InputStream) data;
      try {
        return Dc6.read(handle, stream);
      } finally {
        IOUtils.closeQuietly(stream);
      }
    }
  }

  @Override
  protected Dc6 loadSync0(AssetManager assets, AssetDesc<Dc6> asset, Dc6 dc6) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, dcc: {})", assets, asset, dc6);
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return dc6;
    dc6.uploadTextures(params.direction, params.combineFrames);
    return dc6;
  }
}
