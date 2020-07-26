package com.riiablo.log;

import org.apache.logging.log4j.ThreadContext;

import com.badlogic.gdx.utils.OrderedMap;

// TODO: support ThreadLocal or convert into ThreadContextMap impl
public enum CTX {
  INSTANCE;

  public static void put(String key, String value) {
    ThreadContext.put(key, value);
//    INSTANCE.map.put(key, value);
  }

  public static void remove(String key) {
    ThreadContext.remove(key);
//    INSTANCE.map.remove(key);
  }

  public static void clear() {
    ThreadContext.clearMap();
//    INSTANCE.map.clear();
  }

  @Deprecated
  public static OrderedMap<String, String> map() {
    return null;
//    return INSTANCE.map;
  }

//  final OrderedMap<String, String> map = new OrderedMap<>();
}
