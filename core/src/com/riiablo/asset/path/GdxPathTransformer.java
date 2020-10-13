package com.riiablo.asset.path;

import io.netty.util.AsciiString;

public enum GdxPathTransformer implements PathTransformer {
  INSTANCE;

  @Override
  public AsciiString transform(AsciiString path) {
    return path.replace('\\', '/');
  }
}
