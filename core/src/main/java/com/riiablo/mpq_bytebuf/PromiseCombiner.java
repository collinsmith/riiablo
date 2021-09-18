package com.riiablo.mpq_bytebuf;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copy of {@link io.netty.util.concurrent.PromiseCombiner} with the
 * modification that {@code doneCount} is an {@link AtomicInteger} in order to
 * allow for atomic increments and checks. Maybe I was doing something wrong,
 * but when submitting tasks to an {@link EventExecutorGroup}, aggregating the
 * results and blocking using the standard netty impl, I was getting concurrency
 * errors causing incorrect increments of {@code doneCount} when
 * {@link PromiseCombiner#executor} was {@link io.netty.util.concurrent.ImmediateEventExecutor#INSTANCE}
 *
 * @see <a href="https://github.com/netty/netty/blob/d50cfc69e03a35e984d12d381de7a89ac8f0b2d7/common/src/main/java/io/netty/util/concurrent/PromiseCombiner.java">Netty Github Repository d50cfc6</a>
 */
final class PromiseCombiner {
  private int expectedCount;
  private final AtomicInteger doneCount = new AtomicInteger();
  private Promise<Void> aggregatePromise;
  private Throwable cause;
  private final GenericFutureListener<Future<?>> listener = new GenericFutureListener<Future<?>>() {
    @Override
    public void operationComplete(final Future<?> future) {
      if (executor.inEventLoop()) {
        operationComplete0(future);
      } else {
        executor.execute(new Runnable() {
          @Override
          public void run() {
            operationComplete0(future);
          }
        });
      }
    }

    private void operationComplete0(Future<?> future) {
      assert executor.inEventLoop();
      final int doneCount = PromiseCombiner.this.doneCount.incrementAndGet();
      if (!future.isSuccess() && cause == null) {
        cause = future.cause();
      }
      if (doneCount == expectedCount && aggregatePromise != null) {
        tryPromise();
      }
    }
  };

  private final EventExecutor executor;

  /**
   * The {@link EventExecutor} to use for notifications. You must call {@link #add(Future)}, {@link
   * #addAll(Future[])} and {@link #finish(Promise)} from within the {@link EventExecutor} thread.
   *
   * @param executor
   *     the {@link EventExecutor} to use for notifications.
   */
  public PromiseCombiner(EventExecutor executor) {
    this.executor = ObjectUtil.checkNotNull(executor, "executor");
  }

  /**
   * Adds a new promise to be combined. New promises may be added until an aggregate promise is
   * added via the {@link io.netty.util.concurrent.PromiseCombiner#finish(Promise)} method.
   *
   * @param promise
   *     the promise to add to this promise combiner
   *
   * @deprecated Replaced by {@link io.netty.util.concurrent.PromiseCombiner#add(Future)}.
   */
  @Deprecated
  public void add(Promise promise) {
    add((Future) promise);
  }

  /**
   * Adds a new future to be combined. New futures may be added until an aggregate promise is added
   * via the {@link io.netty.util.concurrent.PromiseCombiner#finish(Promise)} method.
   *
   * @param future
   *     the future to add to this promise combiner
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void add(Future future) {
    checkAddAllowed();
    checkInEventLoop();
    ++expectedCount;
    future.addListener(listener);
  }

  /**
   * Adds new promises to be combined. New promises may be added until an aggregate promise is added
   * via the {@link io.netty.util.concurrent.PromiseCombiner#finish(Promise)} method.
   *
   * @param promises
   *     the promises to add to this promise combiner
   *
   * @deprecated Replaced by {@link io.netty.util.concurrent.PromiseCombiner#addAll(Future[])}
   */
  @Deprecated
  public void addAll(Promise... promises) {
    addAll((Future[]) promises);
  }

  /**
   * Adds new futures to be combined. New futures may be added until an aggregate promise is added
   * via the {@link io.netty.util.concurrent.PromiseCombiner#finish(Promise)} method.
   *
   * @param futures
   *     the futures to add to this promise combiner
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void addAll(Future... futures) {
    for (Future future : futures) {
      this.add(future);
    }
  }

  /**
   * <p>Sets the promise to be notified when all combined futures have finished. If all combined
   * futures succeed,
   * then the aggregate promise will succeed. If one or more combined futures fails, then the
   * aggregate promise will fail with the cause of one of the failed futures. If more than one
   * combined future fails, then exactly which failure will be assigned to the aggregate promise is
   * undefined.</p>
   *
   * <p>After this method is called, no more futures may be added via the {@link
   * io.netty.util.concurrent.PromiseCombiner#add(Future)} or
   * {@link io.netty.util.concurrent.PromiseCombiner#addAll(Future[])} methods.</p>
   *
   * @param aggregatePromise
   *     the promise to notify when all combined futures have finished
   */
  public void finish(Promise<Void> aggregatePromise) {
    ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
    checkInEventLoop();
    if (this.aggregatePromise != null) {
      throw new IllegalStateException("Already finished");
    }
    this.aggregatePromise = aggregatePromise;
    if (doneCount.get() == expectedCount) {
      tryPromise();
    }
  }

  private void checkInEventLoop() {
    if (!executor.inEventLoop()) {
      throw new IllegalStateException("Must be called from EventExecutor thread");
    }
  }

  private boolean tryPromise() {
    return (cause == null) ? aggregatePromise.trySuccess(null) : aggregatePromise.tryFailure(cause);
  }

  private void checkAddAllowed() {
    if (aggregatePromise != null) {
      throw new IllegalStateException("Adding promises is not allowed after finished adding");
    }
  }
}
