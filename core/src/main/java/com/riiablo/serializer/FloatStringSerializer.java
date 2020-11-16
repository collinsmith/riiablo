package com.riiablo.serializer;

import android.support.annotation.NonNull;

public enum FloatStringSerializer implements StringSerializer<Float> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Float obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Float deserialize(@NonNull String string) {
    try {
      return Float.parseFloat(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
