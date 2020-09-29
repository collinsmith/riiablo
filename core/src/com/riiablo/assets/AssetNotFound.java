package com.riiablo.assets;

public class AssetNotFound extends RuntimeException {
  final Asset asset;

  AssetNotFound(Asset asset) {
    this(asset, null);
  }

  AssetNotFound(Asset asset, Throwable cause) {
    super("Asset not found " + asset, cause);
    this.asset = asset;
  }

  public Asset asset() {
    return asset;
  }
}
