package com.riiablo.logger.message;

import java.util.IllegalFormatException;
import java.util.Locale;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FormattedMessage implements Message {
  private final Locale locale;
  private final String pattern;
  private final Object[] args;
  private final Throwable throwable;

  private String formattedMessage;

  public FormattedMessage(final String pattern, final Object... args) {
    this.locale = Locale.getDefault();
    this.pattern = pattern;
    this.args = args;
    this.throwable =
        args != null && args.length > 0
            && args[args.length - 1] instanceof Throwable
        ? (Throwable) args[args.length - 1]
        : null;
  }

  @Override
  public void release() {}

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
    return args.length;
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
