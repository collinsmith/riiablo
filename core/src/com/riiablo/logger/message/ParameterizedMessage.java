package com.riiablo.logger.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class ParameterizedMessage implements Message {
  private static final Pool<StringBuilder> POOL = Pools.get(StringBuilder.class);
  private static final int MAX_BUFFER_SIZE = 255;

  private static StringBuilder obtainStringBuilder() {
    final StringBuilder buffer = POOL.obtain();
    buffer.setLength(0);
    return buffer;
  }

  private static void releaseStringBuilder(final StringBuilder buffer) {
    if (buffer == null) return;
    buffer.setLength(MAX_BUFFER_SIZE);
    buffer.trimToSize();
    POOL.free(buffer);
  }

  private final String pattern;
  private final Object[] args;

  private int[] offsets;
  private int argc;
  private Throwable throwable;

  private String formattedMessage;

  public ParameterizedMessage(final String pattern, final Object... args) {
    this.pattern = pattern;
    this.args = args;
  }

  private void init() {
    assert formattedMessage == null : "formattedMessage(" + formattedMessage + ") != " + null;
    final int len = Math.max(1, pattern == null ? 0 : pattern.length() >> 1);
    this.offsets = new int[len];
    final int argc = ParameterFormatter.countArgs(pattern, offsets);
    this.throwable = argc < args.length && args[args.length - 1] instanceof Throwable
        ? (Throwable) args[args.length - 1]
        : null;
    this.argc = Math.min(argc, args == null ? 0 : args.length);
  }

  @Override
  public String format() {
    if (formattedMessage == null) {
      init();
      final StringBuilder buffer = obtainStringBuilder();
      try {
        formattedMessage = format(buffer).toString();
      } finally {
        releaseStringBuilder(buffer);
      }
    }
    return formattedMessage;
  }

  protected StringBuilder format(final StringBuilder buffer) {
    if (formattedMessage != null) {
      buffer.append(formattedMessage);
    } else {
      ParameterFormatter.format(buffer, pattern, args, argc, offsets);
    }
    return buffer;
  }

  @Override
  public String pattern() {
    return pattern;
  }

  @Override
  public Object[] parameters() {
    return args;
  }

  @Override
  public Throwable throwable() {
    return throwable;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("pattern", pattern)
        .append("args", args)
        .append("throwable", throwable)
        .build();
  }
}
