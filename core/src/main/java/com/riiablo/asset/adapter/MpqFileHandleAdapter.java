package com.riiablo.asset.adapter;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import java.io.InputStream;

import com.riiablo.asset.Adapter;
import com.riiablo.mpq_bytebuf.MpqFileHandle;

public class MpqFileHandleAdapter extends Adapter<MpqFileHandle> {
  public MpqFileHandleAdapter() {
    super(MpqFileHandle.class);
  }

  @Override
  public int defaultBufferSize(MpqFileHandle handle) {
    return handle.sectorSize();
  }

  @Override
  public Future<InputStream> stream(EventExecutor executor, MpqFileHandle handle, int bufferSize) {
    if (bufferSize > 0) {
      return handle.bufferStream(executor, bufferSize);
    } else {
      return executor.newSucceededFuture(handle.stream(true));
    }
  }

  @Override
  public Future<ByteBuf> buffer(EventExecutor executor, MpqFileHandle handle, int offset, int length) {
    return handle.bufferAsync(executor, offset, length);
  }
}
