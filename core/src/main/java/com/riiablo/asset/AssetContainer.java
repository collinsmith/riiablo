package com.riiablo.asset;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.riiablo.util.ImplUtils.unimplemented;

public class AssetContainer extends AbstractReferenceCounted {
  public static AssetContainer wrap(AssetDesc asset, Future<?> future) {
    if (future == null) throw new IllegalArgumentException("future cannot be null");
    return new AssetContainer(asset, future);
  }

  final AssetDesc asset; // for context of which asset this contains
  final Future<?> future;

  AssetContainer(AssetDesc asset, Future<?> future) {
    this.asset = asset;
    this.future = future;
  }

  @SuppressWarnings("unchecked")
  public <T> Future<T> get(Class<T> type) {
    return (Future<T>) future;
  }

  @Override
  protected void deallocate() {
    // dispose if completed, else ?
    // AssetUtils.dispose(ref);
    unimplemented();
  }

  @Override
  public ReferenceCounted touch(Object hint) {
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("future", future)
        .append("refCnt", refCnt())
        .toString();
  }
}
