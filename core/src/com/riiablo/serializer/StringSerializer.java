package com.riiablo.serializer;

import android.support.annotation.NonNull;

public interface StringSerializer<T> extends Serializer<T, String> {
  @NonNull
  String serialize(@NonNull T obj);
  @NonNull
  T deserialize(@NonNull String str);
}
