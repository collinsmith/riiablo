package com.riiablo.asset.adapter;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.FileHandleAdapter;

public class ByteArrayAdapter<F extends FileHandle> implements Adapter<F, byte[]> {
  @Override
  public byte[] get(FileHandleAdapter<F> adapter, F handle) {
    return adapter.readBytes(handle);
  }
}
