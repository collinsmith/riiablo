package com.riiablo.logger;

import org.apache.commons.lang3.StringUtils;

public class LogManager {
  public static final LoggerRegistry INSTANCE = new LoggerRegistry();

  public static final String ROOT = StringUtils.EMPTY;

  public static Logger getLogger(Class<?> clazz) {
    return INSTANCE.getLogger(clazz);
  }

  public static Logger getLogger(String name) {
    return INSTANCE.getLogger(name);
  }

  private LogManager() {}
}
