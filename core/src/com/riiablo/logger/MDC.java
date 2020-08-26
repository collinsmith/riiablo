package com.riiablo.logger;

import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.collections4.map.UnmodifiableOrderedMap;

public class MDC {
  private MDC() {}

  private static final ThreadLocal<OrderedMap<String, String>> threadLocal
      = new ThreadLocal<OrderedMap<String, String>>() {
    @Override
    protected OrderedMap<String, String> initialValue() {
      return new ListOrderedMap<>(); // TODO: copy on write map
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

  public static UnmodifiableOrderedMap<String, String> freeze() {
    return (UnmodifiableOrderedMap<String,String>)
        UnmodifiableOrderedMap.unmodifiableOrderedMap(threadLocal.get());
  }
}
