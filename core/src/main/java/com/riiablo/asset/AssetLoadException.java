package com.riiablo.asset;

public class AssetLoadException extends RuntimeException {
  public final AssetDesc asset;

  public AssetLoadException(AssetDesc asset, String message) {
    super(message);
    this.asset = asset;
  }

  public AssetLoadException(AssetDesc asset, String message, Throwable cause) {
    super(message, cause);
    this.asset = asset;
  }
}
