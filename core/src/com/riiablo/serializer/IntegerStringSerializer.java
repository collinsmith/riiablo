package com.riiablo.serializer;

import android.support.annotation.NonNull;

public enum IntegerStringSerializer implements StringSerializer<Integer> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Integer obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Integer deserialize(@NonNull String obj) {
    try {
      return Integer.parseInt(obj);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
