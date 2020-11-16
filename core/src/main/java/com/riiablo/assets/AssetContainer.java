package com.riiablo.assets;

import io.netty.util.AbstractReferenceCounted;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Disposable;

final class AssetContainer extends AbstractReferenceCounted {
  public static AssetContainer wrap(Asset asset, Object ref) {
    if (ref == null) throw new IllegalArgumentException("ref cannot be null");
    return new AssetContainer(asset, ref);
  }

  final Asset asset;
  Object ref;

  AssetContainer(Asset asset, Object ref) {
    this.asset = asset;
    this.ref = ref;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type) {
    return (T) ref;
  }

  public void set(Object ref) {
    this.ref = ref;
  }

  @Override
  protected void deallocate() {
    if (ref instanceof Disposable) ((Disposable) ref).dispose();
  }

  @Override
  public AssetContainer touch(Object hint) {
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("ref", ref)
        .append("refCnt", refCnt())
        .build();
  }
}
