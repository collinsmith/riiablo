package com.riiablo.asset;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.param.Dt1Params;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map5.Block;
import com.riiablo.map5.Dt1;
import com.riiablo.map5.Dt1Decoder;

import static com.riiablo.util.ImplUtils.unsupported;

/**
 * Utility class to load tile blocks since they are discarded by Dt1Loader
 */
public class BlockLoader extends AssetLoader<Block[]> {
  private static final Logger log = LogManager.getLogger(BlockLoader.class);

  private AssetDesc<Dt1> libraryOf(AssetDesc<Block[]> asset) {
    return AssetDesc.of(asset.path(), Dt1.class, Dt1Params.library());
  }

  @Override
  protected AssetDesc[] dependencies0(AssetDesc<Block[]> asset) {
    BlockParams params = asset.params(BlockParams.class);
    if (params.tileId < 0) return unsupported("tileId must be >= 0");
    AssetDesc<Dt1> header = libraryOf(asset);
    return new AssetDesc[] { header };
  }

  @Override
  protected <F extends FileHandle> Future<?> ioAsync0(
      EventExecutor executor,
      AssetManager assets,
      AssetDesc<Block[]> asset,
      F handle,
      Adapter<F> adapter
  ) {
    log.traceEntry("ioAsync0(executor: {}, asset: {}, handle: {}, adapter: {})", executor, asset, handle, adapter);
    BlockParams params = asset.params(BlockParams.class);
    if (params.tileId >= 0) {
      Dt1 dt1 = assets.getDepNow(libraryOf(asset));
      int offset = dt1.blocksOffset(params.tileId);
      int length = dt1.blocksLength(params.tileId);
      return adapter.buffer(executor, handle, offset, length);
    } else {
      return unsupported("tileId must be >= 0");
    }
  }

  @Override
  protected <F extends FileHandle> Block[] loadAsync0(
      AssetManager assets,
      AssetDesc<Block[]> asset,
      F handle,
      Object data
  ) {
    log.traceEntry("loadAsync0(assets: {}, asset: {}, handle: {}, data: {})", assets, asset, handle, data);
    BlockParams params = asset.params(BlockParams.class);
    if (params.tileId >= 0) {
      boolean released = ReferenceCountUtil.release(handle); // dt1 already owns a reference
      assert !released : handle + " was released, parent dt1 did not retain it";
      Dt1 dt1 = assets.getDepNow(libraryOf(asset));
      assert data instanceof ByteBuf;
      ByteBuf buffer = (ByteBuf) data; // borrowed, don't release
      log.trace("decoding {}", asset);
      Block[] blocks = Dt1Decoder.readBlockHeaders(dt1, params.tileId, ByteInput.wrap(buffer));
      return blocks;
    } else {
      return unsupported("tileId must be >= 0");
    }
  }

  @Override
  protected Block[] loadSync0(AssetManager assets, AssetDesc<Block[]> asset, Block[] blocks) {
    log.traceEntry("loadSync0(assets: {}, asset: {}, blocks: {})", assets, asset, blocks);
    return blocks;
  }
}
