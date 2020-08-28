package com.riiablo.logger;

import org.apache.commons.lang3.StringUtils;

public class LogManager {
  private static final LoggerRegistry INSTANCE = new LoggerRegistry();

  public static final String ROOT = StringUtils.EMPTY;

  public static LoggerRegistry getRegistry() {
    return INSTANCE;
  }

  public static Logger getLogger(Class<?> clazz) {
    return INSTANCE.getLogger(clazz);
  }

  public static Logger getLogger(String name) {
    return INSTANCE.getLogger(name);
  }

  public static Logger getRootLogger() {
    return INSTANCE.getRoot();
  }

  public static Level getLevel(String name) {
    return INSTANCE.getLevel(name);
  }

  public static void setLevel(String name, Level level) {
    INSTANCE.setLevel(name, level);
  }

  public static void setLevel(String name, Level level, boolean force) {
    INSTANCE.setLevel(name, level, force);
  }

  private LogManager() {}
}
