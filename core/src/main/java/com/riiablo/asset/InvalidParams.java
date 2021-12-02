package com.riiablo.asset;

public class InvalidParams extends AssetLoadException {
  public InvalidParams(AssetDesc asset, String message) {
    super(asset, message);
  }

  public InvalidParams(AssetDesc asset, String message, Throwable cause) {
    super(asset, message, cause);
  }
}
