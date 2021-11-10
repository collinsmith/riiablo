package com.riiablo.asset;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssetContainer extends AbstractReferenceCounted {
  static final AssetContainer[] EMPTY_ASSET_CONTAINER_ARRAY = new AssetContainer[0];

  public static AssetContainer wrap(
      AssetDesc asset,
      Promise<?> promise,
      AssetContainer[] dependencies
  ) {
    if (promise == null) throw new IllegalArgumentException("promise cannot be null");
    return new AssetContainer(asset, promise, dependencies);
  }

  final AssetDesc asset; // for context of which asset this contains
  final Promise<?> promise;
  final AssetContainer[] dependencies;

  AssetContainer(
      AssetDesc asset,
      Promise<?> promise,
      AssetContainer[] dependencies
  ) {
    this.asset = asset;
    this.promise = promise;
    this.dependencies = dependencies;
  }

  @SuppressWarnings("unchecked")
  public <T> Future<T> get(Class<T> type) {
    return (Future<T>) promise;
  }

  @Override
  public AssetContainer retain() {
    super.retain();
    return this;
  }

  @Override
  protected void deallocate() {
    // dispose if completed, else ?
    promise.cancel(false);
    if (promise.isDone()) AssetUtils.dispose(promise.getNow());
    for (AssetContainer dependency : dependencies) {
      dependency.release();
    }
  }

  @Override
  public ReferenceCounted touch(Object hint) {
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("future", promise)
        .append("refCnt", refCnt())
        .append("asset", asset)
        .toString();
  }
}
