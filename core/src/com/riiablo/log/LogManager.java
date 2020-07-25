package com.riiablo.log;

import java.util.SortedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.logging.log4j.Logger;

public enum LogManager {
  INSTANCE;

  public static Logger getLogger(Class<?> clazz) {
    return INSTANCE.get(clazz);
  }

  public static Logger getLogger(String name) {
    return INSTANCE.get(name);
  }

  private final Trie<String, Logger> loggers = new PatriciaTrie<>();

  public Logger get(Class<?> clazz) {
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(clazz);
    loggers.put(logger.getName().toLowerCase(), logger);
    return logger;
  }

  public Logger get(String name) {
    Logger logger = org.apache.logging.log4j.LogManager.getLogger(name);
    loggers.put(logger.getName().toLowerCase(), logger);
    return logger;
  }

  public SortedMap<String, Logger> prefixMap(String key) {
    return loggers.prefixMap(key);
  }
}
