package com.riiablo.asset;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AssetContainer extends AbstractReferenceCounted {
  public static AssetContainer wrap(AssetDesc asset, Promise<?> promise) {
    if (promise == null) throw new IllegalArgumentException("promise cannot be null");
    return new AssetContainer(asset, promise);
  }

  final AssetDesc asset; // for context of which asset this contains
  final Promise<?> promise;

  AssetContainer(AssetDesc asset, Promise<?> promise) {
    this.asset = asset;
    this.promise = promise;
  }

  @SuppressWarnings("unchecked")
  public <T> Future<T> get(Class<T> type) {
    return (Future<T>) promise;
  }

  @Override
  protected void deallocate() {
    // dispose if completed, else ?
    promise.cancel(false);
    if (promise.isDone()) AssetUtils.dispose(promise.getNow());
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
        .toString();
  }
}
