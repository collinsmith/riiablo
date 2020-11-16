package com.riiablo.assets;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

public abstract class FileHandleAdapter<F extends FileHandle> {
  final Class<F> type;
  final Map<Class, AsyncAssetLoader.Reader> readers;

  protected FileHandleAdapter(Class<F> type) {
    this.type = type;
    this.readers = new HashMap<>();
    readers.put(type, new AsyncAssetLoader.ReflectiveReader<>());
    readers.put(InputStream.class, new AsyncAssetLoader.InputStreamReader<>());
    readers.put(byte[].class, new AsyncAssetLoader.ByteArrayReader<>());
    readers.put(ByteBuf.class, new AsyncAssetLoader.ByteBufReader<>());
  }

  @SuppressWarnings("unchecked")
  public <V> V adapt(F handle, Class<V> type) {
    return (V) readers.get(type).get(this, handle);
  }

  public abstract byte[] readBytes(F handle);
  public abstract InputStream read(F handle);
  public abstract ByteBuf readByteBuf(F handle);
}
