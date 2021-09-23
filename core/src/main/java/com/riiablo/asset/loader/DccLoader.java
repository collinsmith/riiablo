package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.AssetDesc;
import com.riiablo.asset.AssetLoader;
import com.riiablo.asset.AssetManager;
import com.riiablo.asset.EmptyArray;
import com.riiablo.asset.FileHandleResolver;
import com.riiablo.asset.param.DcParams;
import com.riiablo.file.Dcc;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.util.ImplUtils.unimplemented;

public class DccLoader extends AssetLoader<Dcc> {
  private static final Logger log = LogManager.getLogger(DccLoader.class);

  private static final DcParams PARENT_DC = DcParams.of(-1);

  public DccLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public Array<AssetDesc> dependencies(AssetDesc<Dcc> asset) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) return EmptyArray.empty();
    AssetDesc<Dcc> header = AssetDesc.of(asset, PARENT_DC);
    final Array<AssetDesc> dependencies = Array.of(false, 1, AssetDesc.class);
    dependencies.add(header);
    return dependencies;
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync(
      EventExecutor executor,
      AssetDesc<Dcc> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      return adapter.buffer(executor, handle, 0, 0);
    } else {
      return adapter.stream(executor, handle, adapter.defaultBufferSize(handle));
    }
  }

  @Override
  protected <F extends FileHandle> Dcc loadAsync(
      AssetManager assets,
      AssetDesc<Dcc> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      Dcc parent = assets.load(AssetDesc.of(asset, PARENT_DC)).getNow();
      assert parent != null : "parent dcc should not be null";
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data;
      try {
        // dcc decode data and load params.direction
      } finally {
        ReferenceCountUtil.release(buffer);
      }
      return null;
    } else {
      assert data instanceof InputStream;
      InputStream stream = (InputStream) data;
      try {
        // dcc decode header
        // pass handle reference to DCC
        return Dcc.read(handle, stream);
      } finally {
        ReferenceCountUtil.release(stream);
      }
    }
  }

  @Override
  protected Dcc loadSync(AssetManager assets, AssetDesc<Dcc> asset, Dcc dcc) {
    log.traceEntry("loadSync(assets: {}, asset: {}, dcc: {})", assets, asset, dcc);
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return dcc;
    return unimplemented();
  }
}
