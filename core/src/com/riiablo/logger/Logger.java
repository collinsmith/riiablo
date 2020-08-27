package com.riiablo.logger;

import com.riiablo.logger.message.FormattedMessageFactory;
import com.riiablo.logger.message.Message;
import com.riiablo.logger.message.MessageFactory;
import com.riiablo.logger.message.ParameterizedMessageFactory;

public final class Logger extends AbstractLogger {
  private Appender appender;
  private Level level = Level.WARN;
  private boolean deferred;

  Logger(final String name) {
    this(name, ParameterizedMessageFactory.INSTANCE, FormattedMessageFactory.INSTANCE);
  }

  Logger(
      final String name,
      final MessageFactory defaultFactory,
      final MessageFactory formattedFactory) {
    super(name, defaultFactory, formattedFactory);
  }

  public void addAppender(Appender appender) {
    if (this.appender != null) {
      throw new IllegalStateException("this.appender(" + this.appender + ") != " + null);
    }

    this.appender = appender;
  }

  Appender appender() {
    return appender;
  }

  @Override
  public Level level() {
    return level;
  }

  @Override
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

  @Override
  protected void logIfEnabled(
      final Level level,
      final Message message,
      final StackTraceElement location) {
    if (enabled(level)) {
      log(level, message, location);
    }
  }

  void log(
      final Level level,
      final Message message,
      final StackTraceElement location) {
    final LogEvent event = new LogEvent(level, message, location, MDC.freeze());
    appender.append(event);
  }

  @Override
  public boolean enabled(final Level level) {
    return this.level.isLessSpecificThan(level);
  }
}
