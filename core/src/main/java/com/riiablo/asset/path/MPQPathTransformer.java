package com.riiablo.asset.path;

import com.riiablo.asset.MutableString;

public enum MPQPathTransformer implements PathTransformer {
  INSTANCE;

  private static final byte[] transform = new byte[1 << Byte.SIZE];
  static {
    for (int i = 0; i < transform.length; i++) {
      transform[i] = (byte) i;
    }

    for (int i = 'a'; i <= 'z'; i++) {
      transform[i] &= 0xDf; // 'a' -> 'A'
    }

    transform['/'] = '\\';
  }

  @Override
  public MutableString transform(MutableString path) {
    return path.transform(transform);
  }
}
