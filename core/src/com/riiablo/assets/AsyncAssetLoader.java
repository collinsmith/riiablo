package com.riiablo.assets;

public interface AsyncAssetLoader<T, V> extends AssetLoader<T> {
  void loadAsync(AssetManager assets, Asset<T> asset, V data);
  void unloadAsync(AssetManager assets, Asset<T> asset, V data);
  T loadSync(AssetManager assets, Asset<T> asset, V data);
}
