package com.riiablo.logger;

import com.riiablo.logger.message.Message;

public class LogEvent {
  private final Level level;
  private final Message message;
  private final StackTraceElement source;
  private final StringMap mdc;

  LogEvent(
      Level level,
      Message message,
      StackTraceElement source,
      StringMap mdc) {
    this.level = level;
    this.message = message;
    this.source = source;
    this.mdc = mdc;
  }

  public Level level() {
    return level;
  }

  public Message message() {
    return message;
  }

  public StackTraceElement source() {
    return source;
  }

  public StringMap mdc() {
    return mdc;
  }
}
