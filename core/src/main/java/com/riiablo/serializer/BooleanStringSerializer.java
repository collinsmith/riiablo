package com.riiablo.serializer;

import android.support.annotation.NonNull;

public enum BooleanStringSerializer implements StringSerializer<Boolean> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Boolean obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Boolean deserialize(@NonNull String string) {
    if (string.equalsIgnoreCase("true")) {
      return Boolean.TRUE;
    } else if (string.equalsIgnoreCase("yes")) {
      return Boolean.TRUE;
    } else {
      try {
        int i = Integer.parseInt(string);
        return i > 0 ? Boolean.TRUE : Boolean.FALSE;
      } catch (NumberFormatException e) {
        return Boolean.FALSE;
      }
    }
  }
}
