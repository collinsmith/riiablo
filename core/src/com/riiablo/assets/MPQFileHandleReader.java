package com.riiablo.assets;

import io.netty.buffer.ByteBuf;
import java.util.concurrent.Callable;

import com.riiablo.mpq_bytebuf.MPQFileHandleResolver;

public class MPQFileHandleReader implements AsyncReader<ByteBuf> {
  final MPQFileHandleResolver resolver;

  public MPQFileHandleReader(MPQFileHandleResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public Callable<ByteBuf> readFuture(
      final Asset asset,
      final CatchableCallable.ExceptionHandler exceptionHandler) {
    return CatchableCallable.wrap(new Callable<ByteBuf>() {
      @Override
      public ByteBuf call() {
        return read(asset);
      }
    }, exceptionHandler);
  }

  @Override
  public ByteBuf read(Asset asset) {
    return resolver.resolve(asset.path()).readByteBuf();
  }
}
