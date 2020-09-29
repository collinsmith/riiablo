package com.riiablo.assets.loaders;

import io.netty.buffer.ByteBuf;

import com.badlogic.gdx.utils.Array;

import com.riiablo.assets.Asset;
import com.riiablo.assets.AssetManager;
import com.riiablo.assets.AsyncAssetLoader;
import com.riiablo.assets.FileHandleResolver;
import com.riiablo.assets.MPQFileHandleResolver;
import com.riiablo.codec.DCC;
import com.riiablo.mpq.MPQFileHandle;

public class DCCLoader extends AsyncAssetLoader<DCC, MPQFileHandle, ByteBuf> {
  DCCLoader() {
    super(new ByteBufReader<MPQFileHandle>());
  }

  @Override
  public FileHandleResolver resolver() {
    return new MPQFileHandleResolver();
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

  @Override
  public Array<Asset> getDependencies(Asset<DCC> asset) {
    throw new UnsupportedOperationException();
  }
}
