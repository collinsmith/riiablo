package com.riiablo.logger;

public enum Level {
  OFF(0),
  FATAL(100),
  ERROR(200),
  WARN(300),
  INFO(400),
  DEBUG(500),
  TRACE(600),
  ALL(Integer.MAX_VALUE);

  private final int priority;

  Level(final int priority) {
    if (priority < 0) {
      throw new IllegalArgumentException("priority(" + priority + ") < " + 0);
    }
    this.priority = priority;
  }

  public int priority() {
    return priority;
  }

  public boolean isInRange(final Level min, final Level max) {
    return this.priority >= min.priority && this.priority <= max.priority;
  }

  public boolean isLessSpecificThan(final Level level) {
    return this.priority >= level.priority;
  }

  public boolean isMoreSpecificThan(final Level level) {
    return this.priority <= level.priority;
  }

  public static Level valueOf(String name, Level defaultValue) {
    if (name == null) return defaultValue;
    try {
      return Level.valueOf(name);
    } catch (IllegalArgumentException t) {
      return defaultValue;
    }
  }
}
