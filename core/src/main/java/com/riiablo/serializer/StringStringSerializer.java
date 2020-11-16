package com.riiablo.serializer;

import android.support.annotation.NonNull;

public enum StringStringSerializer implements StringSerializer<String> {
  INSTANCE;

  @NonNull
  @Override
  public String serialize(@NonNull String obj) {
    return obj;
  }

  @NonNull
  @Override
  public String deserialize(@NonNull String str) {
    return str;
  }
}
