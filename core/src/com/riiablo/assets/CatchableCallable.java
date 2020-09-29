package com.riiablo.assets;

import java.util.concurrent.Callable;
import org.apache.commons.lang3.exception.ExceptionUtils;

public final class CatchableCallable<T> implements Callable<T> {
  public static <T> Callable<T> wrap(Callable<T> callable, ExceptionHandler exceptionHandler) {
    return new CatchableCallable<>(callable, exceptionHandler);
  }

  final Callable<T> callable;
  final ExceptionHandler exceptionHandler;

  protected CatchableCallable(Callable<T> callable, ExceptionHandler exceptionHandler) {
    this.callable = callable;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public T call() {
    try {
      return callable.call();
    } catch (Throwable t) {
      exceptionHandler.uncaughtException(Thread.currentThread(), t.getCause());
      return ExceptionUtils.rethrow(t);
    }
  }

  interface ExceptionHandler extends Thread.UncaughtExceptionHandler {}
}
