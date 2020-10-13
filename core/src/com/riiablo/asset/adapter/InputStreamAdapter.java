package com.riiablo.asset.adapter;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.FileHandleAdapter;

public class InputStreamAdapter<F extends FileHandle> implements Adapter<F, InputStream> {
  @Override
  public InputStream get(FileHandleAdapter<F> adapter, F handle) {
    return adapter.read(handle);
  }
}
