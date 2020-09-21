package com.riiablo.logger.message;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;

import com.badlogic.gdx.utils.Pool;

public class PooledFormattedMessage implements Message {
  static final Pool<PooledFormattedMessage> POOL = new Pool<PooledFormattedMessage>() {
    @Override
    protected PooledFormattedMessage newObject() {
      return new PooledFormattedMessage();
    }
  };

  final Object[] args = new Object[10];
  int numArgs;

  Locale locale;
  String pattern;
  Throwable throwable;
  String formattedMessage;

  PooledFormattedMessage() {}

  @Override
  public void release() {
    formattedMessage = null;
    Arrays.fill(args, 0, numArgs, null);
    POOL.free(this);
  }

  @Override
  public String format() {
    return formattedMessage == null
        ? formattedMessage = format(locale, pattern, args)
        : formattedMessage;
  }

  protected static String format(final Locale locale, final String pattern, final Object[] args) {
    try {
      return String.format(locale, pattern, args);
    } catch (final IllegalFormatException t) {
      // TODO: log.error("Unable to format message: " + messagePattern, t);
      return pattern;
    }
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
