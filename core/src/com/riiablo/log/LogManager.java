package com.riiablo.log;

import java.io.File;
import java.io.FileInputStream;
import java.util.SortedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public enum LogManager {
  INSTANCE;

  // DO NOT USE GDX METHODS HERE
  // THIS BLOCK SHOULD BE GDX AGNOSTIC
  static {
    // -Dlog4j.configurationFile=log4j2.xml
    System.out.println("Initializing log4j2 configuration file...");
    try {
      String log4jConfigFile = System.getProperty("user.dir") + File.separator + "log4j2.xml";
      ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jConfigFile));
      Configurator.initialize(null, source);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

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
