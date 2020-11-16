package com.riiablo.asset;

import com.badlogic.gdx.files.FileHandle;

public interface Adapter<F extends FileHandle, V> {
  V get(FileHandleAdapter<F> adapter, F handle);
}
