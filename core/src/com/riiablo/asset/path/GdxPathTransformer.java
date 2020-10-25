package com.riiablo.asset.path;

import com.riiablo.asset.MutableString;

public enum GdxPathTransformer implements PathTransformer {
  INSTANCE;

  @Override
  public MutableString transform(MutableString path) {
    return path.replace('\\', '/');
  }
}
