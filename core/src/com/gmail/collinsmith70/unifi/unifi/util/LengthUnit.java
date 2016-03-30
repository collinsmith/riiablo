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
    @Override public double toPixels(double l)                       { return PPC * toCentimeters(l); }
    @Override public double toMillimeters(double l)                  { return l; }
    @Override public double toCentimeters(double l)                  { return l / 10; }
    @Override public double toMeters(double l)                       { return l / 1000; }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toMillimeters(l); }
  },

  CENTIMETERS {
    @Override public double toPixels(double l)                       { return PPC * l; }
    @Override public double toMillimeters(double l)                  { return l * 10; }
    @Override public double toCentimeters(double l)                  { return l; }
    @Override public double toMeters(double l)                       { return l / 100; }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toCentimeters(l); }
  },

  METERS {
    @Override public double toPixels(double l)                       { return PPC * toCentimeters(l); }
    @Override public double toMillimeters(double l)                  { return l * 1000; }
    @Override public double toCentimeters(double l)                  { return l * 10; }
    @Override public double toMeters(double l)                       { return l; }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toMeters(l); }
  };

  private static final double MAX = Double.MAX_VALUE;
  private static final double PPC = Gdx.graphics.getDensity() * 2.54;

  private enum State {
    LOOKING_FOR_DIGITS,
    PARSING_DIGITS,
    PARSING_WHITESPACE,
    PARSING_METRIC
  }

  /**
   * Number of pixels on the screen which equals the corresponding length and {@code LengthUnit}.
   * Value is expected in the given regular expression: {@code [0-9]+\w*(px|mm|cm|m)}.
   * <ul>
   *   <li>px <i>(pixels)</i></li>
   *   <li>mm <i>(millimeters)</i></li>
   *   <li>cm <i>(centimeters)</i></li>
   *   <li>m <i>(meters)</i></li>
   * </ul>
   *
   * @param value String representation of the length and {@code LengthUnit} to translate
   *
   * @return Number of pixels corresponding with that length and unit
   */
  public static double toPixels(@NonNull final String value) {
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null");
    } else if (value.isEmpty()) {
      throw new IllegalArgumentException("value cannot be empty");
    }

    double sourceLength = 0;
    State state = State.LOOKING_FOR_DIGITS;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      switch (state) {
        case LOOKING_FOR_DIGITS:
          if (ch < '0' || '9' < ch) {
            throw new IllegalArgumentException(
                    "value should match the following regular expression: [0-9]+\\w*(px|dp|mm|cm|m)");
          }

          sourceLength = ch - 48;
          state = State.PARSING_DIGITS;
          break;
        case PARSING_DIGITS:
          if (ch < '0' || '9' < ch) {
            if (Character.isWhitespace(ch)) {
              state = State.PARSING_WHITESPACE;
            } else if (ch == 'c'
                    || ch == 'p'
                    || ch == 'm') {
              i--;
              state = State.PARSING_METRIC;
            }

            break;
          }

          sourceLength *= 10;
          sourceLength += ch - 48;
          break;
        case PARSING_WHITESPACE:
          if (!Character.isWhitespace(ch)) {
            if (ch == 'c'
             || ch == 'p'
             || ch == 'm') {
              i--;
              state = State.PARSING_METRIC;
            } else {
              throw new IllegalArgumentException(
                      "value should match the following regular expression: " +
                              "[0-9]+\\w*(px|dp|mm|cm|m)");
            }

            break;
          }

          break;
        case PARSING_METRIC:
          switch (ch) {
            case 'c':
              if (i + 2 == value.length()
               && value.charAt(i+1) == 'm') {
                return CENTIMETERS.toPixels(sourceLength);
              }

              throw new IllegalArgumentException(
                      "value should match the following regular expression: " +
                              "[0-9]+\\w*(px|dp|mm|cm|m)");
            case 'm':
              if (i + 1 == value.length()) {
                return METERS.toPixels(sourceLength);
              } else if (i + 2 == value.length()
                      && value.charAt(i+1) == 'm') {
                return MILLIMETERS.toPixels(sourceLength);
              }

              throw new IllegalArgumentException(
                      "value should match the following regular expression: " +
                              "[0-9]+\\w*(px|dp|mm|cm|m)");
            case 'p':
              if (i + 2 == value.length()
               && value.charAt(i+1) == 'x') {
                return sourceLength;
              }

              throw new IllegalArgumentException(
                      "value should match the following regular expression: " +
                              "[0-9]+\\w*(px|dp|mm|cm|m)");
            default:
              throw new IllegalArgumentException(
                      "value should match the following regular expression: " +
                              "[0-9]+\\w*(px|dp|mm|cm|m)");
          }
        default:
          throw new IllegalArgumentException(
                  "value should match the following regular expression: " +
                          "[0-9]+\\w*(px|dp|mm|cm|m)");
      }
    }

    throw new IllegalArgumentException(
            "value should match the following regular expression: " +
                    "[0-9]+\\w*(px|dp|mm|cm|m)");
  }

  /**
   * Equivalent to {@code PIXELS.convert(length, this)}.
   *
   * @param length length to convert
   *
   * @return converted length, or {@link Double#POSITIVE_INFINITY} if conversion would overflow,
   *         or {@code 0.0} if it would underflow
   */
  public abstract double toPixels(double length);

  /**
   * Equivalent to {@code MILLIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   *
   * @return converted length, or {@link Double#POSITIVE_INFINITY} if conversion would overflow,
   *         or {@code 0.0} if it would underflow
   *
   * @see #convert(double, LengthUnit)
   */
  public abstract double toMillimeters(double length);

  /**
   * Equivalent to {@code CENTIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   *
   * @return converted length, or {@link Double#POSITIVE_INFINITY} if conversion would overflow,
   *         or {@code 0.0} if it would underflow
   *
   * @see #convert(double, LengthUnit)
   */
  public abstract double toCentimeters(double length);

  /**
   * Equivalent to {@code METERS.convert(length, this)}.
   *
   * @param length length to convert
   *
   * @return converted length, or {@link Double#POSITIVE_INFINITY} if conversion would overflow,
   *         or {@code 0.0} if it would underflow
   *
   * @see #convert(double, LengthUnit)
   */
  public abstract double toMeters(double length);

  /**
   * Converts the given length in the given unit to this unit. Conversions from finer to coarser
   * granularities round, and will lose some precision. For example converting 999 millimeters to
   * meters results in 0.999. Conversions from coarser to finer granularities with arguments that
   * would numerically overflow result in {@link Double#POSITIVE_INFINITY} and {@code 0.0} if it
   * would result in an underflow.
   * <p>
   * For example, to convert 10 meters to millimeters, use:
   * {@code LengthUnit.MILLIMETERS.convert(10L, LengthUnit.METERS)}
   * </p>
   *
   * @param sourceLength length in the given {@code sourceUnit}
   * @param sourceUnit   {@linkplain LengthUnit unit} of the {@code sourceDuration} argument
   *
   * @return converted length, or {@link Double#POSITIVE_INFINITY} if conversion would overflow,
   *         or {@code 0.0} if it would underflow
   */
  public abstract double convert(double sourceLength, @NonNull LengthUnit sourceUnit);

}
