package com.riiablo.asset;

public class InvalidDependency extends AssetLoadException {
  public InvalidDependency(AssetDesc asset, String message) {
    super(asset, message);
  }

  public InvalidDependency(AssetDesc asset, String message, Throwable cause) {
    super(asset, message, cause);
  }
}
