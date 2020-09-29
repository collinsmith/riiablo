package com.riiablo.assets;

import io.netty.util.AsciiString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

/**
 * n io threads perform async io to retrieve ByteBuf or MPQInputStream
 * n async threads perform async loading of ByteBuf to data
 * sync thread created stuff on GL thread
 */
public class AssetManager implements Disposable, LoadTask.Callback, AsyncReader.AsyncHandler {
  private static final Logger log = LogManager.getLogger(AssetManager.class);

  final Map<AsciiString, AssetContainer> assets = new ConcurrentHashMap<>();
  final Map<AsciiString, Array<Asset>> dependencies = new ConcurrentHashMap<>();

  final Map<Class, AssetLoader> loaders = new ConcurrentHashMap<>();
  final ArrayBlockingQueue<LoadTask> completedTasks = new ArrayBlockingQueue<>(256);
  final ArrayList<LoadTask> drain = new ArrayList<>(256);

  final Map<Class, SyncReader> readers = new ConcurrentHashMap<>();

  final ExecutorService io;
  final ExecutorService async;

  volatile int loaded;
  volatile int peakTasks;

  public AssetManager() {
    this(1);
  }

  public AssetManager(int nThreads) {
    final String pool = AssetManager.class.getSimpleName();
    io = Executors.newSingleThreadExecutor(new NamedThreadFactory(pool, "io"));
    async = Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(pool, "async"));
  }

  @Override
  public void dispose() {
    clear();
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

  public AssetLoader getLoader(Class type) {
    return loaders.get(type);
  }

  AssetLoader findLoader(Class type) {
    final AssetLoader loader = getLoader(type);
    if (loader == null) throw new LoaderNotFound(type);
    return loader;
  }

  public <T> void setLoader(Class<T> type, AssetLoader<T> loader) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (loader == null) throw new IllegalArgumentException("loader cannot be null");
    log.debug("Loader set {} -> {}", type.getSimpleName(), loader.getClass());
    loaders.put(type, loader);
  }

  public SyncReader getReader(Class type) {
    return readers.get(type);
  }

  SyncReader findReader(Class type) {
    final SyncReader reader = getReader(type);
    if (reader == null) throw new ReaderNotFound(type);
    return reader;
  }

  public <F extends FileHandle, B> void setReader(Class<F> type, SyncReader<B> reader) {
    if (type == null) throw new IllegalArgumentException("type cannot be null");
    if (reader == null) throw new IllegalArgumentException("reader cannot be null");
    log.debug("Reader set {} -> {}", type.getSimpleName(), reader.getClass());
    readers.put(type, reader);
  }

  public void clear() {
    log.debug("Disposing assets...");

    log.trace("Clearing task queue...");
//    queue.clear();

    log.trace("Completing current tasks...");
    update();

    log.trace("Unloading assets...");
    for (AssetContainer container : assets.values()) {
      unload(container.asset);
    }

    assets.clear(); // assert unloaded
    dependencies.clear(); // assert unloaded
    completedTasks.clear(); // assert unloaded
    drain.clear(); // assert unloaded
  }

  public <T> T get(Asset<T> asset) {
    final AssetContainer container = assets.get(asset.path);
    if (container == null) return null;
    return container.get(asset.type);
  }

  public <T> void unload(Asset<T> asset) {
    log.traceEntry("unload(asset: {})", asset);
  }

  public <T> void load(Asset<T> asset) {
    log.traceEntry("load(asset: {})", asset);
//    findAsset(asset);

    offer(asset);
    log.debug("Queued {}", asset);
  }

  void offer(Asset asset) {
    final AssetContainer container = assets.get(asset.path);
    if (container != null) {
      log.debug("Asset already loaded: {}", container);
      //retain(asset);
      //notifyFinishedLoading(asset);
      loaded++;
      return;
    }

    final AssetLoader loader = findLoader(asset.type);
    final FileHandle handle = loader.resolver().resolve(asset);
    final SyncReader reader = findReader(handle.getClass());
    if (reader instanceof AsyncReader) {
      io.submit(((AsyncReader) reader).readFuture(asset, this));
    } else {
//    io.submit(reader.create(asset));
    }
  }

  @Override
  public void uncaughtException(Thread thread, Throwable t) {
    log.error("{} threw {}", thread.getName(), t.getMessage(), t);
  }

  @Override
  public void onFinishedLoading(Asset asset, Object data) {
    async.submit(new LoadTask(asset, this));
  }

  @Override
  public void onFinishedLoading(LoadTask task, Asset asset) {
    completedTasks.add(task);
  }

  public void update() {
    updateSync();

  }

  void updateSync() {
    assert drain.isEmpty() : "drain not empty: " + drain;
    final int numCompleted = completedTasks.drainTo(drain);
    if (numCompleted > 0) {
      log.debug("{} tasks completed", numCompleted);
      for (final LoadTask task : drain) {
        addAsset(task.asset, task.ref);
      }

      drain.clear();
    }
  }

  <T> void addAsset(Asset<T> asset, T ref) {
    assets.put(asset.path, new AssetContainer(asset, ref));
  }

//  void findAsset(Asset asset) {
//    // check queued assets
//    synchronized (queue) {
//      final Asset[] queue = this.queue.items;
//      for (int i = 0, s = this.queue.size; i < s; i++) {
//        final Asset queued = queue[i];
//        if (queued.path.contentEquals(asset.path) && !queued.type.equals(asset.type)) {
//          throw new AssetTypeMismatch(asset, queued.type);
//        }
//      }
//    }
//
//    // check task queue
//    if (tasks.contains(asset)) {
//    }
//
//    // check loaded assets
//    final AssetContainer container = assets.get(asset.path);
//  }

  private static class NamedThreadFactory implements ThreadFactory {
    final String pool;
    final String type;

    NamedThreadFactory(String pool, String type) {
      this.pool = pool;
      this.type = type;
    }

    @Override
    public Thread newThread(Runnable r) {
        final String name = String.format("%s-%s-%08x", pool, type, MathUtils.random.nextInt());
        log.debug("Creating {}...", name);
        final Thread thread = new Thread(r, name);
        thread.setDaemon(true);
        return thread;
    }
  }
}
