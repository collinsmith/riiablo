package com.riiablo.logger;

import com.riiablo.logger.message.Message;
import com.riiablo.logger.message.MessageFactory;
import com.riiablo.logger.message.PooledFormattedMessageFactory;
import com.riiablo.logger.message.PooledParameterizedMessageFactory;
import com.riiablo.util.Pool;

public final class Logger {
  private static final String FQCN = Logger.class.getName();

  private static final Pool<LogEvent> POOL = new Pool<LogEvent>(true, true) {
    @Override
    protected LogEvent newInstance() {
      return new LogEvent();
    }
  };

  protected final String name;
  protected final MessageFactory defaultFactory;
  protected final MessageFactory formattedFactory;

  private Appender appender;
  private Level level = Level.WARN;
  private boolean deferred;

  Logger(final String name) {
    this(name, PooledParameterizedMessageFactory.INSTANCE, PooledFormattedMessageFactory.INSTANCE);
  }

  Logger(
      final String name,
      final MessageFactory defaultFactory,
      final MessageFactory formattedFactory) {
    this.name = name;
    this.defaultFactory = defaultFactory;
    this.formattedFactory = formattedFactory;
  }

  public final String name() {
    return name;
  }

  public final MessageFactory defaultFactory() {
    return defaultFactory;
  }

  public final MessageFactory formattedFactory() {
    return formattedFactory;
  }

  public void addAppender(Appender appender) {
    this.appender = appender;
  }

  Appender appender() {
    return appender;
  }

  public Level level() {
    return level;
  }

  public void level(final Level level) {
    this.level = level;
    deferred = false;
  }

  boolean deferred() {
    return deferred;
  }

  void bind(Level level, boolean force) {
    if (force) deferred = true;
    if (deferred) this.level = level;
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message, Object... args) {
    if (enabled(level)) {
      log(level, factory.newMessage(message, args), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message, Object arg0) {
    if (enabled(level)) {
      log(level, factory.newMessage(message, arg0), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4,
      Object arg5) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4, arg5), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4,
      Object arg5, Object arg6) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4, arg5, arg6), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4,
      Object arg5, Object arg6, Object arg7) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4,
      Object arg5, Object arg6, Object arg7, Object arg8) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8), getLocation());
    }
  }

  private void logIfEnabled(
      Level level,
      MessageFactory factory,
      String message,
      Object arg0, Object arg1, Object arg2, Object arg3, Object arg4,
      Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    if (enabled(level)) {
      log(level, factory.newMessage(message,
          arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9), getLocation());
    }
  }

  void log(
      final Level level,
      final Message message,
      final StackTraceElement location) {
    final LogEvent event = POOL.obtain();
    event.level = level;
    event.message = message;
    event.source = location;
    event.mdc = MDC.freeze();
    appender.append(event);
  }

  public boolean enabled(final Level level) {
    return this.level.isLessSpecificThan(level);
  }

  public final boolean traceEnabled() {
    return enabled(Level.TRACE);
  }

  public final void trace(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, params);
  }

  public final void trace(String message, Object arg0) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0);
  }

  public final void trace(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void trace(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void tracef(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, params);
  }

  public final void tracef(String message, Object arg0) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0);
  }

  public final void tracef(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void tracef(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final boolean debugEnabled() {
    return enabled(Level.DEBUG);
  }

  public final void debug(final String message, final Object... params) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, params);
  }

  public final void debug(String message, Object arg0) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0);
  }

  public final void debug(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void debug(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.DEBUG, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void debugf(final String message, final Object... params) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, params);
  }

  public final void debugf(String message, Object arg0) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0);
  }

  public final void debugf(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void debugf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.DEBUG, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final boolean infoEnabled() {
    return enabled(Level.INFO);
  }

  public final void info(final String message, final Object... params) {
    logIfEnabled(Level.INFO, defaultFactory(), message, params);
  }

  public final void info(String message, Object arg0) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0);
  }

  public final void info(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void info(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.INFO, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void infof(final String message, final Object... params) {
    logIfEnabled(Level.INFO, formattedFactory(), message, params);
  }

  public final void infof(String message, Object arg0) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0);
  }

  public final void infof(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void infof(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.INFO, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final boolean warnEnabled() {
    return enabled(Level.WARN);
  }

  public final void warn(final String message, final Object... params) {
    logIfEnabled(Level.WARN, defaultFactory(), message, params);
  }

  public final void warn(String message, Object arg0) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0);
  }

  public final void warn(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void warn(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.WARN, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void warnf(final String message, final Object... params) {
    logIfEnabled(Level.WARN, formattedFactory(), message, params);
  }

  public final void warnf(String message, Object arg0) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0);
  }

  public final void warnf(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void warnf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.WARN, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final boolean errorEnabled() {
    return enabled(Level.ERROR);
  }

  public final void error(final String message, final Object... params) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, params);
  }

  public final void error(String message, Object arg0) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0);
  }

  public final void error(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void error(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.ERROR, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void errorf(final String message, final Object... params) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, params);
  }

  public final void errorf(String message, Object arg0) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0);
  }

  public final void errorf(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void errorf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.ERROR, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final boolean fatalEnabled() {
    return enabled(Level.FATAL);
  }

  public final void fatal(final String message, final Object... params) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, params);
  }

  public final void fatal(String message, Object arg0) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0);
  }

  public final void fatal(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void fatal(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.FATAL, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void fatalf(final String message, final Object... params) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, params);
  }

  public final void fatalf(String message, Object arg0) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0);
  }

  public final void fatalf(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void fatalf(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.FATAL, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void traceEntry() {
    final StackTraceElement location = getLocation();
    logIfEnabled(Level.TRACE, defaultFactory(), location.getMethodName());
  }

  public final void traceEntry(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, params);
  }

  public final void traceEntry(String message, Object arg0) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0);
  }

  public final void traceEntry(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void traceEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.TRACE, defaultFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  public final void tracefEntry(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, params);
  }

  public final void tracefEntry(String message, Object arg0) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  public final void tracefEntry(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    logIfEnabled(Level.TRACE, formattedFactory(), message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }

  private StackTraceElement getLocation() {
    return getLocation(FQCN);
  }

  /**
   * Borrowed from Log4j2
   */
  private StackTraceElement getLocation(final String fqcn) {
    if (fqcn == null) {
      return null;
    }

    final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    boolean found = false;
    for (int i = 0; i < stackTrace.length; i++) {
      final String className = stackTrace[i].getClassName();
      if (fqcn.equals(className)) {
        found = true;
        continue;
      }
      if (found && !fqcn.equals(className)) {
        return stackTrace[i];
      }
    }
    return null;
  }
}
