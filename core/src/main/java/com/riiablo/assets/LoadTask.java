package com.riiablo.assets;

final class LoadTask implements Runnable {
  final Asset asset;
  final Callback callback;

  Object ref;

  public LoadTask(Asset asset, Callback callback) {
    this.asset = asset;
    this.callback = callback;
  }

  @Override
  public void run() {
    //...
    callback.onFinishedLoading(this, asset);
  }

  public interface Callback {
    void onFinishedLoading(LoadTask task, Asset asset);
  }
}
