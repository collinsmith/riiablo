package com.riiablo.assets;

import io.netty.util.AsciiString;

import com.badlogic.gdx.files.FileHandle;

public class MPQFileHandleResolver extends FileHandleResolver {
  public MPQFileHandleResolver() {
    super(MPQPathTransformer.INSTANCE);
  }

  @Override
  protected FileHandle resolveTransformed(AsciiString path) {
    throw new UnsupportedOperationException();
  }
}
