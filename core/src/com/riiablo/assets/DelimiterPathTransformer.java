package com.riiablo.assets;

import io.netty.util.AsciiString;

public enum DelimiterPathTransformer implements PathTransformer {
  INSTANCE;

  @Override
  public AsciiString transform(AsciiString path) {
    return path.replace('\\', '/');
  }
}
