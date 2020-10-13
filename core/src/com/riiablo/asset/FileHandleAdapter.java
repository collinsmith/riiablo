package com.riiablo.asset;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.adapter.ByteArrayAdapter;
import com.riiablo.asset.adapter.ByteBufAdapter;
import com.riiablo.asset.adapter.InputStreamAdapter;
import com.riiablo.asset.adapter.ReflectiveAdapter;

public abstract class FileHandleAdapter<F extends FileHandle> {
  final Class<F> type;
  final Map<Class, Adapter> adapters;

  protected FileHandleAdapter(Class<F> type) {
    this.type = type;
    this.adapters = new HashMap<>();
    adapters.put(type, new ReflectiveAdapter());
    adapters.put(InputStream.class, new InputStreamAdapter<>());
    adapters.put(byte[].class, new ByteArrayAdapter<>());
    adapters.put(ByteBuf.class, new ByteBufAdapter<>());
  }

  @SuppressWarnings("unchecked")
  public <V> V adapt(F handle, Class<V> type) {
    return (V) adapters.get(type).get(this, handle);
  }

  public abstract byte[] readBytes(F handle);
  public abstract InputStream read(F handle);
  public abstract ByteBuf readByteBuf(F handle);
}
