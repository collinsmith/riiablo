package com.riiablo.assets;

import java.util.concurrent.Callable;

public interface AsyncReader<B> {
  B read(Asset asset);
  Callable<B> readFuture(Asset asset, CatchableCallable.ExceptionHandler exceptionHandler);
}
