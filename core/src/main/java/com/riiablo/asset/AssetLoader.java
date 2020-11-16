package com.riiablo.asset;

import com.badlogic.gdx.utils.Array;

public class AssetLoader<T, V> {
  final FileHandleResolver resolver;
  final Class<V> type;

  protected AssetLoader(FileHandleResolver resolver, Class<V> type) {
    this.resolver = resolver;
    this.type = type;
  }

  public final Class<V> type() {
    return type;
  }

  public final FileHandleResolver resolver() {
    return resolver;
  }

  public Array<AssetDesc> dependencies(AssetDesc asset) {
    return null;
  }
}
