package com.riiablo.assets;

import java.util.concurrent.Callable;

public interface AsyncReader<B> extends SyncReader<B> {
  @Override
  B read(Asset asset);
  Callable<B> readFuture(Asset asset, AsyncHandler exceptionHandler);

  interface AsyncHandler<B> extends CatchableCallable.ExceptionHandler {
    void onFinishedLoading(Asset asset, B data);
  }
}
