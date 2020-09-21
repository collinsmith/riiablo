package com.riiablo.logger.message;

import java.util.Arrays;

import com.badlogic.gdx.utils.Pool;

public class PooledParameterizedMessage implements Message {
  private static final int MAX_BUFFER_SIZE = 255;
  private static final Pool<StringBuilder> BUFFER_POOL = new Pool<StringBuilder>() {
    @Override
    protected StringBuilder newObject() {
      return new StringBuilder(MAX_BUFFER_SIZE);
    }
  };

  private static StringBuilder obtainStringBuilder() {
    final StringBuilder buffer = BUFFER_POOL.obtain();
    buffer.setLength(0);
    return buffer;
  }

  private static void releaseStringBuilder(final StringBuilder buffer) {
    if (buffer == null) return;
    buffer.setLength(MAX_BUFFER_SIZE);
    buffer.trimToSize();
    BUFFER_POOL.free(buffer);
  }

  static final Pool<PooledParameterizedMessage> POOL = new Pool<PooledParameterizedMessage>() {
    @Override
    protected PooledParameterizedMessage newObject() {
      return new PooledParameterizedMessage();
    }
  };

  final Object[] args = new Object[10];
  int numArgs;
  private int[] offsets = new int[10];
  private int argc;

  String pattern;
  Throwable throwable;
  String formattedMessage;

  PooledParameterizedMessage() {}

  private void init() {
    assert formattedMessage == null : "formattedMessage(" + formattedMessage + ") != " + null;
    final int len = Math.max(1, pattern == null ? 0 : pattern.length() >> 1);
    this.offsets = new int[len];
    final int argc = ParameterFormatter.countArgs(pattern, offsets);
    this.throwable = argc < numArgs && args[numArgs - 1] instanceof Throwable
        ? (Throwable) args[numArgs - 1]
        : null;
    this.argc = Math.min(argc, args == null ? 0 : numArgs);
  }

  @Override
  public void release() {
    formattedMessage = null;
    Arrays.fill(args, 0, numArgs, null);
    POOL.free(this);
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
  public int numParameters() {
    return numArgs;
  }

  @Override
  public Throwable throwable() {
    return throwable;
  }
}
