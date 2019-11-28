package com.riiablo.map;

import com.badlogic.gdx.utils.Pool;

public class ByteArrayPool extends Pool<byte[]> {
  final int size;

  public ByteArrayPool(int size) {
    this.size = size;
  }

  @Override
  protected byte[] newObject() {
    return new byte[size];
  }
}
