package com.riiablo.assets;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;

import com.riiablo.mpq_bytebuf.MPQFileHandle;

public class MPQFileHandleAdapter implements FileHandleAdapter<MPQFileHandle>, AsyncAdapter {
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
