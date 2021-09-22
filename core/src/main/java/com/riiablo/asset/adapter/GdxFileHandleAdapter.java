package com.riiablo.asset.adapter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;

public class GdxFileHandleAdapter extends Adapter<FileHandle> {
  public GdxFileHandleAdapter() {
    super(FileHandle.class);
  }

  @Override
  public int defaultBufferSize(FileHandle handle) {
    return 4096;
  }

  @Override
  public Future<InputStream> stream(EventExecutor executor, FileHandle handle, int bufferSize) {
    return executor.newSucceededFuture(bufferSize == 0 ? handle.read() : handle.read(bufferSize));
  }

  @Override
  public Future<ByteBuf> buffer(EventExecutor executor, FileHandle handle, int offset, int length) {
    return executor.newSucceededFuture(Unpooled.wrappedBuffer(handle.readBytes(), offset, length));
  }
}
