package com.riiablo.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ParameterException extends RuntimeException {
  public ParameterException() {
    super();
  }

  public ParameterException(@Nullable String message) {
    super(message);
  }

  public ParameterException(@NonNull String format, @Nullable Object... args) {
    super(String.format(format, args));
  }
}