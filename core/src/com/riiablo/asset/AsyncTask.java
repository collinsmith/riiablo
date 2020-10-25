package com.riiablo.asset;

class AsyncTask implements Runnable {
  final AssetDesc asset;
  final Callback callback;

  AsyncTask(AssetDesc asset, Callback callback) {
    this.asset = asset;
    this.callback = callback;
  }

  @Override
  public void run() {
    //...
    callback.onAsyncTaskFinished(this, asset);
  }

  public interface Callback {
    void onAsyncTaskFinished(AsyncTask task, AssetDesc asset);
  }
}
