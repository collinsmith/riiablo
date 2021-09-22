package com.riiablo.asset;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

public abstract class Adapter<F extends FileHandle> {
  protected final Class<F> type;

  protected Adapter(Class<F> type) {
    this.type = type;
  }

  public final Class<F> type() {
    return type;
  }

  public abstract int defaultBufferSize(F handle);
  public abstract Future<InputStream> stream(EventExecutor executor, F handle, int bufferSize);
  public abstract Future<ByteBuf> buffer(EventExecutor executor, F handle, int offset, int length);
}
