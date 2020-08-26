package com.riiablo.logger;

import com.riiablo.logger.message.Message;

public class LogEvent {
  private final Level level;
  private final Message message;
  private final StackTraceElement source;

  LogEvent(Level level, Message message, StackTraceElement source) {
    this.level = level;
    this.message = message;
    this.source = source;
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
}
