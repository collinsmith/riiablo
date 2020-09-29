package com.riiablo.assets;

import com.badlogic.gdx.utils.Array;

public interface AssetLoader<T> {
  Array<Asset> getDependencies(Asset<T> asset);
}
