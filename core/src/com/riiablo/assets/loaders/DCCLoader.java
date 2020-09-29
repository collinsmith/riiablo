package com.riiablo.assets.loaders;

import com.riiablo.assets.Asset;
import com.riiablo.assets.AssetManager;
import com.riiablo.assets.AsyncAssetLoader;
import com.riiablo.assets.FileHandleResolver;
import com.riiablo.codec.DCC;

import io.netty.buffer.ByteBuf;

public class DCCLoader extends AsyncAssetLoader<DCC, ByteBuf> {
  public DCCLoader(FileHandleResolver resolver) {
    super(resolver, ByteBuf.class);
  }

  @Override
  public void loadAsync(AssetManager assets, Asset<DCC> asset, ByteBuf data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unloadAsync(AssetManager assets, Asset<DCC> asset, ByteBuf data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DCC loadSync(AssetManager assets, Asset<DCC> asset, ByteBuf data) {
    throw new UnsupportedOperationException();
  }
}
