package com.riiablo.assets;

import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;

import io.netty.buffer.ByteBuf;

public final class GdxFileHandleAdapter extends FileHandleAdapter<FileHandle> {
  public GdxFileHandleAdapter() {
    super(FileHandle.class);
  }

  @Override
  public byte[] readBytes(FileHandle handle) {
    return handle.readBytes();
  }

  @Override
  public InputStream read(FileHandle handle) {
    return handle.read();
  }

  @Override
  public ByteBuf readByteBuf(FileHandle handle) {
    throw new UnsupportedOperationException();
  }
}
