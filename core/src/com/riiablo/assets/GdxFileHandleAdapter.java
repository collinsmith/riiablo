package com.riiablo.assets;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

public class GdxFileHandleAdapter implements FileHandleAdapter<FileHandle> {
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
