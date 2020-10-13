package com.riiablo.asset.adapter;

import io.netty.buffer.ByteBuf;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.FileHandleAdapter;

public class ByteBufAdapter<F extends FileHandle> implements Adapter<F, ByteBuf> {
  @Override
  public ByteBuf get(FileHandleAdapter<F> adapter, F handle) {
    return adapter.readByteBuf(handle);
  }
}
