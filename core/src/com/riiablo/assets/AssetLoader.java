package com.riiablo.assets;

import com.badlogic.gdx.utils.Array;

public interface AssetLoader<T> {
  FileHandleResolver resolver();
  Array<Asset> getDependencies(Asset<T> asset);
}
