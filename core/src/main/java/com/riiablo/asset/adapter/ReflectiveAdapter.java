package com.riiablo.asset.adapter;

import com.badlogic.gdx.files.FileHandle;

import com.riiablo.asset.Adapter;
import com.riiablo.asset.FileHandleAdapter;

public class ReflectiveAdapter<F extends FileHandle> implements Adapter<F, F> {
  @Override
  public F get(FileHandleAdapter<F> adapter, F handle) {
    return handle;
  }
}
