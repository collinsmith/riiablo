package com.riiablo.assets;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;

import io.netty.buffer.ByteBuf;

public abstract class AsyncAssetLoader<T, V> extends AssetLoader<T, V> {
  public AsyncAssetLoader(FileHandleResolver resolver, Class<V> type) {
    super(resolver, type);
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
