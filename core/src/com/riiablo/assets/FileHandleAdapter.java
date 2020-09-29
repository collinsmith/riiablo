package com.riiablo.assets;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

public interface FileHandleAdapter<T extends FileHandle> {
  byte[] readBytes(T handle);
  InputStream read(T handle);
  ByteBuf readByteBuf(T handle);
}
