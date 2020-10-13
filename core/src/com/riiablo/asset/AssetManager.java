package com.riiablo.asset;

import io.netty.util.AsciiString;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.asset.adapter.GdxFileHandleAdapter;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class AssetManager implements Disposable {
  private static final Logger log = LogManager.getLogger(AssetManager.class);

  final ExecutorService io;
  final ExecutorService async;

  final Map<String, AssetContainer> assets = new ConcurrentHashMap<>();
  final Map<Class, AssetLoader> loaders = new ConcurrentHashMap<>();
  final Map<Class, FileHandleAdapter> adapters = new ConcurrentHashMap<>();

  final Array<PriorityContainer<FileHandleResolver>> resolvers = new Array<>();

  public AssetManager() {
    this(2);
  }

  public AssetManager(int nThreads) {
    final String className = AssetManager.class.getSimpleName();
    io = Executors.newSingleThreadExecutor(new NamedThreadFactory(className, "io"));
    async = Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(className, "async"));

    setAdapter(FileHandle.class, new GdxFileHandleAdapter());
  }

  public AssetLoader getLoader(Class type) {
    return loaders.get(type);
  }

  AssetLoader findLoader(Class type) {
    final AssetLoader loader = getLoader(type);
    if (loader == null) throw new LoaderNotFound(type);
    return loader;
  }

  public <T> void setLoader(Class<T> type, AssetLoader<T, ?> loader) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (loader == null) throw new IllegalArgumentException("loader cannot be null");
    log.debug("Loader set {} -> {}", type.getSimpleName(), loader.getClass());
    loaders.put(type, loader);
  }

  public FileHandleAdapter getAdapter(Class type) {
    return adapters.get(type);
  }

  FileHandleAdapter findAdapter(Class type) {
    final FileHandleAdapter adapter = getAdapter(type);
    if (adapter == null) throw new AdapterNotFound(type);
    return adapter;
  }

  public <F extends FileHandle> void setAdapter(Class<F> type, FileHandleAdapter<F> adapter) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (adapter == null) throw new IllegalArgumentException("adapter cannot be null");
    log.debug("Adapter set {} -> {}", type.getSimpleName(), adapter.getClass());
    adapters.put(type, adapter);
  }

  public void addResolver(FileHandleResolver resolver) {
    addResolver(resolver, Integer.MIN_VALUE);
  }

  public void addResolver(FileHandleResolver resolver, int priority) {
    if (resolver == null) throw new IllegalArgumentException("resolver cannot be null");
    log.debug("Resolver set {}", resolver);
    resolvers.add(PriorityContainer.wrap(priority, resolver));
    resolvers.sort();
  }

  public FileHandle resolve(AsciiString path) {
    for (PriorityContainer<FileHandleResolver> container : resolvers) {
      final FileHandle handle = container.ref.resolve(path);
      if (handle != null) {
        return handle;
      }
    }

    throw new ResolverNotFound(path);
  }

  public <T> T get(AssetDesc<T> asset) {
    final AssetContainer container = assets.get(asset.path());
    if (container == null) return null;
    return container.get(asset.type);
  }

  public <T> T load(AssetDesc<T> asset) {
    log.traceEntry("load(asset: {})", asset);

    final AssetContainer container = assets.get(asset.path());
    if (container != null) {
      log.debug("Asset already loaded: {}", container);
      return container.get(asset.type);
    }

    FileHandle handle;
    try {
      handle = resolve(asset.path);
    } catch (ResolverNotFound t) {
      final AssetLoader loader = findLoader(asset.type);
      loader.resolver().transformer().transform(asset.path);
      handle = null;
    }

    // need series of resolver to try and locate file
    // need to resolve path at this point...

    throw null;
  }

  public <T> void unload(AssetDesc<T> asset) {
    log.traceEntry("unload(asset: {})", asset);
  }

  @Override
  public void dispose() {
    shutdown(io, "io");
    shutdown(async, "async");
  }

  private static void shutdown(ExecutorService executor, String name) {
    log.trace("Shutting down {} executor...", name);
    try {
      executor.shutdown();
      if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
        List<Runnable> unexecutedTasks = executor.shutdownNow();
        log.warn("{} tasks terminated", unexecutedTasks.size());
        if (!executor.awaitTermination(16, TimeUnit.SECONDS)) {
          log.error("{} executor failed to shut down gracefully", name);
        }
      }
    } catch (InterruptedException t) {
      log.error("{} executor failed to shut down gracefully", name, t);
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private static class NamedThreadFactory implements ThreadFactory {
    final String parent;
    final String pool;

    NamedThreadFactory(String parent, String pool) {
      this.parent = parent;
      this.pool = pool;
    }

    @Override
    public Thread newThread(final Runnable r) {
      final String name = String.format("%s-%s-%08x", parent, pool, MathUtils.random.nextInt());
      log.debug("Creating {}...", name);
      final Thread thread = new Thread(r, name);
      thread.setDaemon(true);
      return thread;
    }
  }
}
