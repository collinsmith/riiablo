package com.riiablo.logger;

import com.riiablo.logger.message.Message;
import com.riiablo.logger.message.MessageFactory;

abstract class AbstractLogger {
  private static final String FQCN = AbstractLogger.class.getName();

  protected final String name;
  protected final MessageFactory defaultFactory;
  protected final MessageFactory formattedFactory;

  AbstractLogger(
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

  public abstract Level level();
  public abstract void level(final Level level);
  public abstract boolean enabled(final Level level);

  protected abstract void logIfEnabled(final Level level, final Message message, final StackTraceElement location);

  public final boolean traceEnabled() {
    return enabled(Level.TRACE);
  }

  public final void trace(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void tracef(final String message, final Object... params) {
    logIfEnabled(Level.TRACE, formattedFactory().newMessage(message, params), getLocation());
  }

  public final boolean debugEnabled() {
    return enabled(Level.DEBUG);
  }

  public final void debug(final String message, final Object... params) {
    logIfEnabled(Level.DEBUG, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void debugf(final String message, final Object... params) {
    logIfEnabled(Level.DEBUG, formattedFactory().newMessage(message, params), getLocation());
  }

  public final boolean infoEnabled() {
    return enabled(Level.INFO);
  }

  public final void info(final String message, final Object... params) {
    logIfEnabled(Level.INFO, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void infof(final String message, final Object... params) {
    logIfEnabled(Level.INFO, formattedFactory().newMessage(message, params), getLocation());
  }

  public final boolean warnEnabled() {
    return enabled(Level.WARN);
  }

  public final void warn(final String message, final Object... params) {
    logIfEnabled(Level.WARN, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void warnf(final String message, final Object... params) {
    logIfEnabled(Level.WARN, formattedFactory().newMessage(message, params), getLocation());
  }

  public final boolean errorEnabled() {
    return enabled(Level.ERROR);
  }

  public final void error(final String message, final Object... params) {
    logIfEnabled(Level.ERROR, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void errorf(final String message, final Object... params) {
    logIfEnabled(Level.ERROR, formattedFactory().newMessage(message, params), getLocation());
  }

  public final boolean fatalEnabled() {
    return enabled(Level.FATAL);
  }

  public final void fatal(final String message, final Object... params) {
    logIfEnabled(Level.FATAL, defaultFactory().newMessage(message, params), getLocation());
  }

  public final void fatalf(final String message, final Object... params) {
    logIfEnabled(Level.FATAL, formattedFactory().newMessage(message, params), getLocation());
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
