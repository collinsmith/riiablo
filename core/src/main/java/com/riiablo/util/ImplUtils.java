package com.riiablo.util;

public final class ImplUtils {
  private ImplUtils() {}

  public static <T> T todo() {
    throw new RuntimeException("not yet implemented");
  }

  public static <T> T unimplemented() {
    throw new RuntimeException("not implemented");
  }

  public static <T> T unsupported(String message) {
    throw new UnsupportedOperationException(message);
  }
}
