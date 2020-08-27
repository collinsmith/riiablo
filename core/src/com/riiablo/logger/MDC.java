package com.riiablo.logger;

public class MDC {
  private MDC() {}

  private static final ThreadLocal<StringMap> threadLocal
      = new ThreadLocal<StringMap>() {
    @Override
    protected StringMap initialValue() {
      return new StringMap();
    }
  };

  public static void put(String key, int value) {
    put(key, String.valueOf(value));
  }

  public static void put(String key, String value) {
    threadLocal.get().put(key, value);
  }

  public static String get(String key) {
    return threadLocal.get().get(key);
  }

  public static void remove(String key) {
    threadLocal.get().remove(key);
  }

  public static void clear() {
    threadLocal.get().clear();
  }

  public static int size() {
    return threadLocal.get().size();
  }

  public static StringMap freeze() {
    return threadLocal.get().freeze();
  }
}
