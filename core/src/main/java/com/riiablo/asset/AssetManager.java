package com.riiablo.asset;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import com.riiablo.concurrent.PromiseCombiner;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

import static com.riiablo.asset.AssetContainer.EMPTY_PROMISE_ARRAY;

public final class AssetManager implements Disposable {
  private static final Logger log = LogManager.getLogger(AssetManager.class);

  final ObjectMap<AssetDesc, AssetContainer> loadedAssets = new ObjectMap<>();
  final ObjectMap<Class, AssetLoader> loaders = new ObjectMap<>();
  final ObjectMap<Class, Adapter> adapters = new ObjectMap<>();
  final Array<PriorityContainer<FileHandleResolver>> resolvers = new Array<>();
  final ObjectMap<Class, Class<? extends AssetParams>> defaultParams = new ObjectMap<>();
  final BlockingQueue<SyncMessage> syncQueue = new LinkedBlockingQueue<>();

  final EventExecutorGroup async;
  final EventExecutor sync;

  FileHandleResolver[] resolverCache; // ref updated when resolvers changed

  public AssetManager() {
    async = new DefaultEventExecutorGroup(4);
    sync = ImmediateEventExecutor.INSTANCE;
  }

  @Override
  public void dispose() {
    log.trace("Shutting down async executor...");
    async.shutdownGracefully();

    log.trace("Disposing file handle resolvers...");
    for (FileHandleResolver resolver : PriorityContainer.unwrap(resolvers)) {
      AssetUtils.dispose(resolver);
    }
    resolvers.clear();
    resolverCache = PriorityContainer.toArray(resolvers, FileHandleResolver.class);
  }

  public AssetLoader getLoader(Class type) {
    return loaders.get(type);
  }

  AssetLoader findLoader(Class type) throws LoaderNotFound {
    final AssetLoader loader = getLoader(type);
    if (loader == null) throw new LoaderNotFound(type);
    return loader;
  }

  public <T> AssetManager loader(Class<T> type, AssetLoader<T> loader) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (loader == null) throw new IllegalArgumentException("loader cannot be null");
    log.debug("Loader set {} -> {}", type.getSimpleName(), loader.getClass());
    loaders.put(type, loader);
    return this;
  }

  public AssetManager resolver(FileHandleResolver resolver) {
    return resolver(resolver, 0);
  }

  public AssetManager resolver(FileHandleResolver resolver, int priority) {
    if (resolver == null) throw new IllegalArgumentException("resolver cannot be null");
    log.debug("Resolver added {}", resolver);
    resolvers.add(PriorityContainer.wrap(priority, resolver));
    resolvers.sort(); // stable sort, order maintained for equal priorities
    resolverCache = PriorityContainer.toArray(resolvers, FileHandleResolver.class);
    return this;
  }

  FileHandle resolve(AssetDesc asset) throws ResolverNotFound {
    if (asset.params == null) asset.params = defaultParams(asset.type);
    for (FileHandleResolver resolver : resolverCache) {
      final FileHandle handle = resolver.resolve(asset);
      if (handle != null) return handle;
    }

    throw new ResolverNotFound(asset);
  }

  public Adapter getAdapter(Class type) {
    return adapters.get(type);
  }

  Adapter findAdapter(FileHandle handle) throws AdapterNotFound {
    return findAdapter(handle.getClass());
  }

  Adapter findAdapter(Class type) throws AdapterNotFound {
    final Adapter adapter = getAdapter(type);
    if (adapter == null) throw new AdapterNotFound(type);
    return adapter;
  }

  public <F extends FileHandle> AssetManager adapter(Class<F> type, Adapter<? extends F> adapter) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (adapter == null) throw new IllegalArgumentException("adapter cannot be null");
    log.debug("Adapter set {} -> {}", type.getSimpleName(), adapter.getClass());
    adapters.put(type, adapter);
    return this;
  }

  public <T> AssetManager paramResolver(Class<T> type, Class<? extends AssetParams<? super T>> paramsType) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (paramsType == null) throw new IllegalArgumentException("paramsType cannot be null");
    defaultParams.put(type, paramsType);
    log.debug("Params set {} -> {}", type, paramsType);
    return this;
  }

  AssetParams defaultParams(Class type) throws ParamsNotFound {
    try {
      return defaultParams.get(type).newInstance();
    } catch (Throwable t) {
      throw new ParamsNotFound(type, t);
    }
  }

  // TODO: something more elegant for dependencies
  public <T> T getDepNow(final AssetDesc<T> asset) {
    final AssetContainer container = loadedAssets.get(asset);
    final T object = container != null ? container.get(asset.type).getNow() : null;
    if (object == null) throw new RuntimeException("dependency not loaded: " + asset);
    return object;
  }

  <T> Promise[] loadDependencies(Promise<T> promise, AssetContainer container) {
    final AssetDesc[] dependencies = container.dependencies;
    final int numDependencies = dependencies.length;
    if (numDependencies == 0) return EMPTY_PROMISE_ARRAY;
    Promise[] promises = new Promise[numDependencies];
    for (int i = 0; i < numDependencies; i++) {
      final AssetDesc dependency = dependencies[i];
      // redirection to suppress unchecked warning via variable assignment
      @SuppressWarnings("unchecked") // dependencies submitted by loader
      Promise<?> p = promises[i] = load(dependency);
      if (p.isDone() && p.cause() != null) {
        promise.tryFailure(new InvalidDependency(
            container.asset, "Failed to load one or more dependencies.", p.cause()));
      }
    }
    return promises;
  }

  @SuppressWarnings("unchecked") // guaranteed by loader and adapter contracts
  <T> void ioAsync(final EventExecutor executor, final AssetContainer container) {
    final AssetDesc asset = container.asset;
    final Promise promise = container.promise;
    final AssetLoader loader = findLoader(asset.type);
    final FileHandle handle = resolve(asset); // TODO: refactor AssetLoader#resolver?
    final Adapter adapter = findAdapter(handle);
    loader
        .ioAsync(promise, executor, AssetManager.this, asset, handle, adapter)
        .addListener((FutureListener) future -> {
          @SuppressWarnings("unchecked") // guaranteed by loader contract
          T object = (T) loader.loadAsync(promise, AssetManager.this, asset, handle, future.getNow());
          boolean inserted = syncQueue.offer(SyncMessage.wrap(container, promise, loader, object));
          if (!inserted) log.error("Failed to enqueue {}", asset);
        });
  }

  public <T> Promise<? extends T> load(final AssetDesc<T> asset) {
    log.traceEntry("load(asset: {})", asset);

    final AssetContainer container0 = loadedAssets.get(asset);
    if (container0 != null) return container0.retain().get(asset.type);

    final Promise<T> promise = sync.newPromise();
    promise.setUncancellable();
    promise.addListener(future -> {
      final Throwable cause = future.cause();
      if (cause != null) {
        log.warn("Failed to load asset {}", asset, cause);
      }
    });

    final AssetDesc[] dependencies;
    try {
      dependencies = findLoader(asset.type).dependencies(promise, asset);
    } catch (Throwable t) {
      return promise;
    }

    final AssetContainer container = AssetContainer.wrap(asset, promise, dependencies);
    final Promise[] promises = loadDependencies(promise, container);
    loadedAssets.put(asset, container);
    if (promise.isDone()) return promise; // one or more dependencies was invalid

    try {
      findLoader(asset.type).validate(promise, asset);
    } catch (Throwable t) {
      return promise;
    }

    final EventExecutor executor = async.next();
    executor.execute(() -> {
      if (dependencies.length == 0) {
        ioAsync(executor, container);
      } else {
        PromiseCombiner combiner = new PromiseCombiner(executor);
        for (Promise dependency : promises) {
          combiner.add((Future) dependency);
        }

        Promise<Void> combinerPromise = executor.newPromise();
        combinerPromise.addListener((FutureListener<Void>) future -> ioAsync(executor, container));
        combiner.finish(combinerPromise);
      }
    });

    return promise;
  }

  public void unload(AssetDesc asset) {
    final AssetContainer container = loadedAssets.get(asset);
    if (container == null) return;
    boolean released = container.release();
    if (released) loadedAssets.remove(asset);
    for (AssetDesc dependency : container.dependencies) {
      unload(dependency);
    }
  }

  public void sync(final long timeoutMillis) {
    log.debug("Syncing...");
    SyncMessage msg;
    long timeoutRemaining = timeoutMillis;
    long start, end = System.currentTimeMillis();
    try {
      while (timeoutRemaining > 0) {
        log.debug("taking... ({}ms remaining)", timeoutRemaining);
        start = end;
        msg = syncQueue.poll(timeoutRemaining, TimeUnit.MILLISECONDS);
        if (msg == null) break;
        try {
          msg.loadSync(this);
        } catch (Throwable ignored) {
        }
        end = System.currentTimeMillis();
        timeoutRemaining -= (end - start);
      }
    } catch (InterruptedException t) {
      log.debug(ExceptionUtils.getRootCauseMessage(t), t);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * blocks and processes sync events until the specified asset has loaded
   */
  public void await(AssetDesc asset) throws InterruptedException {
    log.traceEntry("await(asset: {})", asset);
    AssetContainer container = loadedAssets.get(asset);
    if (container != null && container.promise.isDone()) return;
    SyncMessage msg;
    do {
      msg = syncQueue.take();
      msg.loadSync(this);
    } while (!asset.equals(msg.container.asset));
  }

  /**
   * blocks and processes sync events until all specified assets have loaded
   */
  public void awaitAll(AssetDesc... array) throws InterruptedException {
    log.traceEntry("awaitAll(array: {})", (Object) array);
    Array<AssetDesc> assets = Array.with(array);
    Array.ArrayIterator<AssetDesc> it = new Array.ArrayIterator<>(assets);
    while (it.hasNext()) {
      AssetDesc asset = it.next();
      AssetContainer container = loadedAssets.get(asset);
      if (container != null && container.promise.isDone()) {
        it.remove();
      }
    }

    SyncMessage msg;
    while (!assets.isEmpty()) {
      msg = syncQueue.take();
      msg.loadSync(this);
      assets.removeValue(msg.container.asset, false);
    }
  }

  /**
   * blocks and processes sync messages up to a max timeoutMillis,
   * final message will not be interrupted and may exceed timeoutMillis
   */
  public void syncAwait(long timeoutMillis) {
    log.traceEntry("syncAwait(timeoutMillis: {})", timeoutMillis);
    SyncMessage msg;
    long currentTime = System.currentTimeMillis();
    final long endTime = currentTime + timeoutMillis;
    while (currentTime < endTime && (msg = syncQueue.poll()) != null) {
      msg.loadSync(this);
      currentTime = System.currentTimeMillis();
    }
  }
}
