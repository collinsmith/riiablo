package com.gmail.collinsmith70.unifi.unifi.util;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;

/**
 * A {@code LengthUnit} represents the most <a href="https://en.wikipedia.org/wiki/Length">extended
 * dimension of an object</a> at a given unit of granularity and provides utility methods to convert
 * across units. A {@code LengthUnit} does not maintain length information, but only helps organize
 * and use time representations that may be maintained separately across various contexts. A
 * {@linkplain #MILLIMETERS millimeter} is defined as one thousandth of a {@linkplain #METERS
 * meter}, and a {@linkplain #CENTIMETERS centimeter} is defined as one hundredth of a meter.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Length">Wikipedia Article on Length</a>
 * @see <a href="https://en.wikipedia.org/wiki/Metre">Wikipedia Article on Metric System Metre</a>
 */
public enum LengthUnit {

  MILLIMETERS {
    @Override public long toMillimeters(long l)         { return l; }
    @Override public long toCentimeters(long l)         { return l/(C1/C0); }
    @Override public long toMeters(long l)              { return l/(C2/C0); }
    @Override public long convert(long l, LengthUnit u) { return u.toMillimeters(l); }
  },

  CENTIMETERS {
    @Override public long toMillimeters(long l)         { return x(l, C1/C0, MAX/(C1/C0)); }
    @Override public long toCentimeters(long l)         { return l; }
    @Override public long toMeters(long l)              { return l/(C2/C1); }
    @Override public long convert(long l, LengthUnit u) { return u.toCentimeters(l); }
  },

  METERS {
    @Override public long toMillimeters(long l)         { return x(l, C2/C0, MAX/(C2/C0)); }
    @Override public long toCentimeters(long l)         { return x(l, C2/C1, MAX/(C2/C1)); }
    @Override public long toMeters(long l)              { return l; }
    @Override public long convert(long l, LengthUnit u) { return u.toMeters(l); }
  };

  private static final long C0 = 1L;
  private static final long C1 = C0 * 100L;
  private static final long C2 = C0 * 1000L;

  private static final long MAX = Long.MAX_VALUE;

  public static long toPixels(@NonNull String value) {
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null");
    } else if (value.isEmpty()) {
      throw new IllegalArgumentException("value cannot be empty");
    } else if (!value.matches("[0-9]+\\w*(px|mm|cm|m)")) {
      throw new IllegalArgumentException(
              "value must be given as a number followed by either px, mm, cm, or m");
    }

    char ch;
    long sourceLength = 0L;
    for (int i = 0; i < value.length(); i++) {
      ch = value.charAt(i);
      switch (ch) {
        case '0':case '1':case '2':case '3':case '4':
        case '5':case '6':case '7':case '8':case '9':
          sourceLength *= 10;
          sourceLength += (ch - 48);
        case 'c':
          return toPixels(sourceLength, CENTIMETERS);
        case 'm':
          if (i == value.length()) {
            return toPixels(sourceLength, METERS);
          } else {
            return toPixels(sourceLength, MILLIMETERS);
          }
        case 'p':
          return sourceLength;
        default:
          throw new IllegalStateException(
                  "statement has encountered an unexpected character: " + ch);
      }
    }

    throw new IllegalStateException("failed to locate and return source unit");
  }

  public static long toPixels(long sourceLength, LengthUnit sourceUnit) {
    return toPixelsX(sourceLength, sourceUnit);
  }

  public static long toPixelsX(long sourceLength, LengthUnit sourceUnit) {
    long mms = sourceUnit.toMillimeters(sourceLength);
    return (long)(Gdx.graphics.getPpcX() * (double)mms/10);
  }

  public static long toPixelsY(long sourceLength, LengthUnit sourceUnit) {
    long mms = sourceUnit.toMillimeters(sourceLength);
    return (long)(Gdx.graphics.getPpcY() * (double)mms/10);
  }

  /**
   * Scale l by m, checking for overflow. This has a short name to make above code more readable.
   * <p>Note: Taken from {@link java.util.concurrent.TimeUnit#x(long, long, long)}</p>
   */
  static long x(long l, long m, long over) {
    if (l > over) {
      return Long.MAX_VALUE;
    } else if (l < -over) {
      return Long.MIN_VALUE;
    }

    return l * m;
  }

  /**
   * Equivalent to {@code MILLIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(long, LengthUnit)
   */
  public abstract long toMillimeters(long length);

  /**
   * Equivalent to {@code CENTIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(long, LengthUnit)
   */
  public abstract long toCentimeters(long length);

  /**
   * Equivalent to {@code METERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(long, LengthUnit)
   */
  public abstract long toMeters(long length);

  /**
   * Converts the given length in the given unit to this unit. Conversions from finer to coarser
   * granularities truncate, and will lose some precision. For example converting 999 millimeters to
   * meters results in 0. Conversions from coarser to finer granularities with arguments that would
   * numerically overflow to Long.MIN_VALUE if negative or Long.MAX_VALUE if positive.
   * <p>
   * For example, to convert 10 meters to millimeters, use:
   * {@code LengthUnit.MILLIMETERS.convert(10L, LengthUnit.METERS)}
   * </p>
   *
   * @param sourceLength length in the given {@code sourceUnit}
   * @param sourceUnit   {@linkplain LengthUnit unit} of the {@code sourceDuration} argument
   * @return converted duration in this unit, or {@link Long#MIN_VALUE} if conversion would negatively
   * overflow, or {@link Long#MAX_VALUE} if it would positively overflow
   */
  public abstract long convert(long sourceLength, LengthUnit sourceUnit);

}
