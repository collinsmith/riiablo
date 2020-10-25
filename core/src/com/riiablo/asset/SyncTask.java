package com.riiablo.asset;

class SyncTask implements Runnable {
  final AssetDesc asset;
  final Callback callback;

  SyncTask(AssetDesc asset, Callback callback) {
    this.asset = asset;
    this.callback = callback;
  }

  @Override
  public void run() {
    //...
    callback.onSyncTaskFinished(this, asset);
  }

  public interface Callback {
    void onSyncTaskFinished(SyncTask task, AssetDesc asset);
  }
}
