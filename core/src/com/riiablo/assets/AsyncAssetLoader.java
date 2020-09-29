package com.riiablo.assets;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

public abstract class AsyncAssetLoader<T, F extends FileHandle, V> implements AssetLoader<T> {
  final Reader<F, V> reader;

  public AsyncAssetLoader(Reader<F, V> reader) {
    this.reader = reader;
  }

  protected Reader<F, V> reader() {
    return reader;
  }

  void loadAsync(AssetManager assets, Asset<T> asset, FileHandleAdapter<F> adapter, F handle) {
    loadAsync(assets, asset, reader.get(adapter, handle));
  }

  void unloadAsync(AssetManager assets, Asset<T> asset, FileHandleAdapter<F> adapter, F handle) {
    unloadAsync(assets, asset, reader.get(adapter, handle));
  }

  T loadSync(AssetManager assets, Asset<T> asset, FileHandleAdapter<F> adapter, F handle) {
    return loadSync(assets, asset, reader.get(adapter, handle));
  }

  protected abstract void loadAsync(AssetManager assets, Asset<T> asset, V data);
  protected abstract void unloadAsync(AssetManager assets, Asset<T> asset, V data);
  protected abstract T loadSync(AssetManager assets, Asset<T> asset, V data);

  public interface Reader<F extends FileHandle, V> {
    V get(FileHandleAdapter<F> adapter, F handle);
  }

  public static final class InputStreamReader<F extends FileHandle> implements Reader<F, InputStream> {
    @Override
    public InputStream get(FileHandleAdapter<F> adapter, F handle) {
      return adapter.read(handle);
    }
  }

  public static final class ByteArrayReader<F extends FileHandle> implements Reader<F, byte[]> {
    @Override
    public byte[] get(FileHandleAdapter<F> adapter, F handle) {
      return adapter.readBytes(handle);
    }
  }

  public static final class ByteBufReader<F extends FileHandle> implements Reader<F, ByteBuf> {
    @Override
    public ByteBuf get(FileHandleAdapter<F> adapter, F handle) {
      return adapter.readByteBuf(handle);
    }
  }

  public static final class ReflectiveReader<F extends FileHandle> implements Reader<F, F> {
    @Override
    public F get(FileHandleAdapter<F> adapter, F handle) {
      return handle;
    }
  }
}
