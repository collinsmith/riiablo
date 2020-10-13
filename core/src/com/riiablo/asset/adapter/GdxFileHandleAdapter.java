package com.riiablo.asset.adapter;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.FileHandleAdapter;

public class GdxFileHandleAdapter extends FileHandleAdapter<FileHandle> {
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
