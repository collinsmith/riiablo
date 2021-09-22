package com.riiablo.asset.loader;

import io.netty.buffer.ByteBuf;
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
import com.riiablo.codec.DCC;

import static com.riiablo.util.ImplUtils.unimplemented;

public class DccLoader extends AssetLoader<DCC> {
  private static final DcParams PARENT_DC = DcParams.of(-1);

  public DccLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public Array<AssetDesc> dependencies(AssetDesc<DCC> asset) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) return EmptyArray.empty();
    AssetDesc<DCC> header = AssetDesc.of(asset, PARENT_DC);
    final Array<AssetDesc> dependencies = Array.of(false, 1, AssetDesc.class);
    dependencies.add(header);
    return dependencies;
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync(
      EventExecutor executor,
      AssetDesc<DCC> asset,
      F handle,
      Adapter<F> adapter
  ) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      return adapter.buffer(executor, handle, 0, 0);
    } else {
      return adapter.stream(executor, handle, adapter.defaultBufferSize(handle));
    }
  }

  @Override
  protected <F extends FileHandle> DCC loadAsync(
      AssetManager assets,
      AssetDesc<DCC> asset,
      F handle,
      Object data
  ) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction >= 0) {
      DCC parent = assets.load(AssetDesc.of(asset, PARENT_DC)).getNow();
      assert parent != null : "parent dcc should not be null";
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data;
      // dcc decode data and load params.direction
      return null;
    } else {
      assert data instanceof InputStream;
      InputStream stream = (InputStream) data;
      // dcc decode header
      return null;
    }
  }

  @Override
  protected DCC loadSync(AssetManager assets, AssetDesc<DCC> asset, DCC dcc) {
    DcParams params = asset.params(DcParams.class);
    if (params.direction < 0) return dcc;
    return unimplemented();
  }
}
