package com.riiablo.asset;

public class ResolverNotFound extends RuntimeException {
  public final AssetDesc<?> asset;

  ResolverNotFound(AssetDesc<?> asset) {
    this(asset, null);
  }

  ResolverNotFound(AssetDesc<?> asset, Throwable cause) {
    super("Resolver not found for " + asset, cause);
    this.asset = asset;
  }

  public AssetDesc<?> asset() {
    return asset;
  }
}
