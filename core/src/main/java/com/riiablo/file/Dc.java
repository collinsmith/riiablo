package com.riiablo.file;

import io.netty.util.ReferenceCountUtil;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public abstract class Dc implements Disposable {
  protected final FileHandle handle;
  protected final int numDirections;
  protected final int numFrames;

  protected Dc(FileHandle handle, int numDirections, int numFrames) {
    this.handle = handle;
    this.numDirections = numDirections;
    this.numFrames = numFrames;
  }

  @Override
  public void dispose() {
    ReferenceCountUtil.release(handle);
  }

  public FileHandle handle() {
    return handle;
  }

  public int numDirections() {
    return numDirections;
  }

  public int numFrames() {
    return numFrames;
  }
}
