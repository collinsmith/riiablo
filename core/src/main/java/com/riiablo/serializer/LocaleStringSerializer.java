package com.riiablo.serializer;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.LocaleUtils;

import java.util.Locale;

public enum LocaleStringSerializer implements StringSerializer<Locale> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Locale locale) {
    return locale.toString();
  }

  @Override
  @NonNull
  public Locale deserialize(@NonNull String string) {
    try {
      return LocaleUtils.toLocale(string);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }
}
