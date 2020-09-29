package com.riiablo.assets;

import io.netty.util.AsciiString;

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
  public AsciiString transform(AsciiString path) {
    final byte[] transform = MPQPathTransformer.transform;
    boolean changed = false;
    final byte[] array = path.array();
    for (int i = path.arrayOffset(), s = i + path.length(); i < s; i++) {
      final byte b = array[i];
      if (b != (array[i] = transform[b])) changed = true;
    }

    if (changed) path.arrayChanged();
    return path;
  }
}
