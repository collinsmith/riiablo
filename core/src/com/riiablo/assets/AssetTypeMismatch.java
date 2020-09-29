package com.riiablo.assets;

public class AssetTypeMismatch extends RuntimeException {
  AssetTypeMismatch(Asset asset, Class type) {
    this(asset, type, null);
  }

  AssetTypeMismatch(Asset asset, Class type, Throwable cause) {
    super("Asset under '" + asset.path + "' already loaded, but type doesn't match " +
        "(expected: " + type.getSimpleName() + ", found: " + asset.type.getSimpleName() + ")",
        cause);
  }
}
