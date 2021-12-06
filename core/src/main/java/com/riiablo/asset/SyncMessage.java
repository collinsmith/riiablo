package com.riiablo.asset;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

final class SyncMessage<T> {
  static <T> SyncMessage<T> wrap(
      AssetContainer container,
      Promise<T> promise,
      AssetLoader loader,
      T object
  ) {
    return new SyncMessage<>(container, promise, loader, object);
  }

  final AssetContainer container;
  final Promise<T> promise;
  final AssetLoader loader;
  final T object;

  SyncMessage(
      AssetContainer container,
      Promise<T> promise,
      AssetLoader loader,
      T object
  ) {
    assert container.promise == promise : "container.promise != promise";
    this.container = container;
    this.promise = promise;
    this.loader = loader;
    this.object = object;
  }

  @SuppressWarnings("unchecked") // guaranteed by loader contract
  Future<?> loadSync(AssetManager assets) {
    loader.loadSync(promise, assets, container.asset, object);
    promise.trySuccess(object);
    return promise;
  }

  @Override
  public String toString() {
    return container.asset.toString();
  }
}
