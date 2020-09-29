package com.riiablo.assets;

import com.riiablo.mpq_bytebuf.MPQFileHandle;

import java.io.InputStream;

import io.netty.buffer.ByteBuf;

public final class MPQFileHandleAdapter extends FileHandleAdapter<MPQFileHandle> implements AsyncAdapter {
  public MPQFileHandleAdapter() {
    super(MPQFileHandle.class);
  }

  @Override
  public byte[] readBytes(MPQFileHandle handle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream read(MPQFileHandle handle) {
    return handle.read();
  }

  @Override
  public ByteBuf readByteBuf(MPQFileHandle handle) {
    return handle.readByteBuf();
  }
}
