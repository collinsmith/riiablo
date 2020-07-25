package com.riiablo.log;

import org.apache.logging.log4j.ThreadContext;

import com.badlogic.gdx.utils.OrderedMap;

// TODO: support ThreadLocal or convert into ThreadContextMap impl
public enum CTX {
  INSTANCE;

  public static String put(String key, String value) {
    ThreadContext.put(key, value);
    return INSTANCE.map.put(key, value);
  }

  public static String remove(String key) {
    ThreadContext.remove(key);
    return INSTANCE.map.remove(key);
  }

  public static void clear() {
    ThreadContext.clearMap();
    INSTANCE.map.clear();
  }

  public static OrderedMap<String, String> map() {
    return INSTANCE.map;
  }

  final OrderedMap<String, String> map = new OrderedMap<>();
}
