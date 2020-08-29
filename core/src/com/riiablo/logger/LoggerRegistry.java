package com.riiablo.logger;

import java.util.SortedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.TrieUtils;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

public class LoggerRegistry {
  private static final char PACKAGE_SEPARATOR = ClassUtils.PACKAGE_SEPARATOR_CHAR;

  public static final String ROOT = LogManager.ROOT;

  LoggerRegistry() {}

  private final Trie<String, Logger> loggers = new PatriciaTrie<>();
  private final Trie<String, Level> contexts = new PatriciaTrie<>();
  private final Logger ROOT_LOGGER = createRootLogger();

  private Logger createRootLogger() {
    final Logger root = new Logger(ROOT);
    root.level(Level.WARN);
    root.addAppender(new OutputStreamAppender(System.out));
    loggers.put(ROOT, root);
    contexts.put(ROOT, root.level());
    return root;
  }

  public Logger getRoot() {
    return ROOT_LOGGER;
  }

  public Logger getLogger(Class<?> clazz) {
    return getLogger(clazz.getCanonicalName());
  }

  public Logger getLogger(String name) {
    if (ROOT_LOGGER.appender() == null) {
      throw new IllegalStateException("Cannot initialize logger without ROOT appender set!");
    }

    Logger logger = loggers.get(name);
    if (logger == null) {
      loggers.put(name, logger = new Logger(name));
      logger.addAppender(ROOT_LOGGER.appender());
      final Level context = resolve(logger.name());
      logger.bind(context, true);
    }

    return logger;
  }

  public Level getLevel(String name) {
    return resolve(name);
  }

  Level resolve(String name) {
    Level context = contexts.get(name);
    if (context != null) return context;
    for (int i = name.length();
         (i = StringUtils.lastIndexOf(name, PACKAGE_SEPARATOR, i - 1)) != -1; ) {
      context = contexts.get(name.substring(0, i));
      if (context != null) return context;
    }

    assert contexts.get(ROOT) == ROOT_LOGGER.level();
    return ROOT_LOGGER.level();
  }

  public void setLevel(String name, Level level) {
    setLevel(name, level, false);
  }

  public void setLevel(String name, Level level, boolean force) {
    bind(name, level, force);
    bindAll(name, level, force);
  }

  void bind(String name, Level context, boolean force) {
    contexts.put(name, context);
    final Logger logger = loggers.get(name);
    if (logger != null) logger.bind(context, force);
  }

  void bindAll(String pkg, Level context, boolean force) {
    pkg = StringUtils.appendIfMissing(pkg, String.valueOf(PACKAGE_SEPARATOR));
    for (SortedMap.Entry<String, Level> entry
        : contexts.prefixMap(pkg).entrySet()) {
      entry.setValue(context);
    }

    for (Logger logger
        : loggers.prefixMap(pkg).values()) {
      logger.bind(context, force);
    }
  }

  public Trie<String, Level> getContexts() {
    return TrieUtils.unmodifiableTrie(contexts);
  }

  public Trie<String, Logger> getLoggers() {
    return TrieUtils.unmodifiableTrie(loggers);
  }
}
