package com.riiablo.asset;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.util.Arrays;
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

public final class AssetManager implements Disposable {
  private static final Logger log = LogManager.getLogger(AssetManager.class);

  final ObjectMap<AssetDesc, AssetContainer> loadedAssets = new ObjectMap<>();
  final ObjectMap<Class, AssetLoader> loaders = new ObjectMap<>();
  final ObjectMap<Class, Adapter> adapters = new ObjectMap<>();
  final Array<PriorityContainer<FileHandleResolver>> resolvers = new Array<>();
  final ObjectMap<Class, Class<? extends AssetParams>> defaultParams = new ObjectMap<>();
  final BlockingQueue<SyncMessage> syncQueue = new LinkedBlockingQueue<>();

  final EventExecutor deps;
  final EventExecutor io;
  final EventExecutor sync;

  public AssetManager() {
    deps = new DefaultEventExecutor();
    io = new DefaultEventExecutor();
    sync = ImmediateEventExecutor.INSTANCE;
  }

  @Override
  public void dispose() {
    log.trace("Shutting down dependency event executor...");
    deps.shutdownGracefully();

    log.trace("Shutting down i/o event executor...");
    io.shutdownGracefully();

    log.trace("Disposing file handle resolvers...");
    for (FileHandleResolver resolver : PriorityContainer.unwrap(resolvers)) {
      AssetUtils.dispose(resolver);
    }
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
    return this;
  }

  FileHandle resolve(AssetDesc asset) throws ResolverNotFound {
    if (asset.params == null) asset.params = defaultParams(asset.type);
    for (FileHandleResolver resolver : PriorityContainer.unwrap(resolvers)) {
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

  <T> AssetContainer[] loadDependencies(AssetDesc<T> asset) {
    final AssetLoader loader = findLoader(asset.type);
    @SuppressWarnings("unchecked") // guaranteed by loader contract
    Array<AssetDesc> dependencies = loader.dependencies(asset);
    final int numDependencies = dependencies.size;
    final AssetContainer[] containers = numDependencies > 0
        ? new AssetContainer[dependencies.size]
        : AssetContainer.EMPTY_ASSET_CONTAINER_ARRAY;
    for (int i = 0; i < numDependencies; i++) {
      final AssetDesc dependency = dependencies.get(i);
      @SuppressWarnings("unchecked") // dependencies submitted by loader
      AssetContainer container = load0(dependency);
      containers[i] = container;
    }
    return containers;
  }

  <T> void ioAsync(final AssetContainer container) {
    final AssetDesc asset = container.asset;
    final Promise promise = container.promise;
    final AssetLoader loader = findLoader(asset.type);
    final FileHandle handle = resolve(asset); // TODO: refactor AssetLoader#resolver?
    final Adapter adapter = findAdapter(handle);
    io.execute(new Runnable() {
      @Override
      @SuppressWarnings("unchecked") // guaranteed by loader and adapter contracts
      public void run() {
        loader
            .ioAsync(io, AssetManager.this, asset, handle, adapter)
            .addListener(new FutureListener() {
              @Override
              public void operationComplete(Future future) {
                @SuppressWarnings("unchecked") // guaranteed by loader contract
                T object = (T) loader.loadAsync(AssetManager.this, asset, handle, future.getNow());
                // if (loader.requiresSync()) {
                // }
                boolean inserted = syncQueue.offer(SyncMessage.wrap(container, promise, loader, object));
                if (!inserted) log.error("Failed to enqueue {}", asset);
              }
            });
      }
    });
  }

  <T> AssetContainer load0(final AssetDesc<T> asset) {
    log.traceEntry("load0(asset: {})", asset);
    final AssetContainer container0 = loadedAssets.get(asset);
    if (container0 != null) {
      return container0.retain();
    }

    final Promise<T> promise = sync.newPromise();
    final AssetContainer[] dependencies = loadDependencies(asset);
    final AssetContainer container = AssetContainer.wrap(asset, promise, dependencies);
    loadedAssets.put(asset, container);

    // FIXME: below is a workaround since Object[] isn't supported at the moment of writing
    if (log.traceEnabled()) log.trace("Dependencies: {}", Arrays.toString(dependencies));
    if (dependencies.length > 0) {
      deps.execute(new Runnable() {
        @Override
        public void run() {
          PromiseCombiner combiner = new PromiseCombiner(deps);
          for (AssetContainer dependency : dependencies) {
            combiner.add((Future) dependency.promise);
          }

          Promise<Void> combinerPromise = deps.newPromise();
          combinerPromise.addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) {
              ioAsync(container);
            }
          });
          combiner.finish(combinerPromise);
        }
      });
    } else {
      ioAsync(container);
    }

    return container;
  }

  public <T> Future<T> load(final AssetDesc<T> asset) {
    return load0(asset).get(asset.type);
  }

  public void unload(AssetDesc asset) {
    final AssetContainer container = loadedAssets.get(asset);
    if (container == null) return;
    boolean released = container.release();
    if (released) loadedAssets.remove(asset);
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
        msg.loadSync(this);
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
    do {
      msg = syncQueue.take();
      msg.loadSync(this);
      assets.removeValue(msg.container.asset, false);
    } while (!assets.isEmpty());
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
