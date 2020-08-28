package com.riiablo.logger;

import org.apache.commons.lang3.StringUtils;

public class LogManager {
  private static final LoggerRegistry DEFAULT_REGISTRY = new LoggerRegistry();

  public static final String ROOT = StringUtils.EMPTY;

  public static LoggerRegistry getRegistry() {
    return DEFAULT_REGISTRY;
  }

  public static Logger getLogger(Class<?> clazz) {
    return DEFAULT_REGISTRY.getLogger(clazz);
  }

  public static Logger getLogger(String name) {
    return DEFAULT_REGISTRY.getLogger(name);
  }

  public static Logger getRootLogger() {
    return DEFAULT_REGISTRY.getRoot();
  }

  public static Level getLevel(String name) {
    return DEFAULT_REGISTRY.getLevel(name);
  }

  public static void setLevel(String name, Level level) {
    DEFAULT_REGISTRY.setLevel(name, level);
  }

  public static void setLevel(String name, Level level, boolean force) {
    DEFAULT_REGISTRY.setLevel(name, level, force);
  }

  private LogManager() {}
}
