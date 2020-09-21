package com.riiablo.logger;

import com.riiablo.logger.message.Message;

public final class LogEvent {
  Level level;
  Message message;
  StackTraceElement source;
  StringMap mdc;

  LogEvent() {}

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

  public void release() {
    message.release();
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
