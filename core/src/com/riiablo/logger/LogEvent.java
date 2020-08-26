package com.riiablo.logger;

import org.apache.commons.collections4.map.UnmodifiableOrderedMap;

import com.riiablo.logger.message.Message;

public class LogEvent {
  private final Level level;
  private final Message message;
  private final StackTraceElement source;
  private final UnmodifiableOrderedMap<String, String> mdc;

  LogEvent(
      Level level,
      Message message,
      StackTraceElement source,
      UnmodifiableOrderedMap<String, String> mdc) {
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

  public UnmodifiableOrderedMap<String, String> mdc() {
    return mdc;
  }
}
