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
    @Override public double toMillimeters(double l)                  { return l; }
    @Override public double toCentimeters(double l)                  { return l/(C1/C0); }
    @Override public double toMeters(double l)                       { return l/(C2/C0); }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toMillimeters(l); }
  },

  CENTIMETERS {
    @Override public double toMillimeters(double l)                  { return x(l, C1/C0, MAX/(C1/C0)); }
    @Override public double toCentimeters(double l)                  { return l; }
    @Override public double toMeters(double l)                       { return l/(C2/C1); }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toCentimeters(l); }
  },

  METERS {
    @Override public double toMillimeters(double l)                  { return x(l, C2/C0, MAX/(C2/C0)); }
    @Override public double toCentimeters(double l)                  { return x(l, C2/C1, MAX/(C2/C1)); }
    @Override public double toMeters(double l)                       { return l; }
    @Override public double convert(double l, @NonNull LengthUnit u) { return u.toMeters(l); }
  };

  private static final double C0 = 1.0;
  private static final double C1 = C0 * 100.0;
  private static final double C2 = C0 * 1000.0;

  private static final double MAX = Double.MAX_VALUE;
  private static final double PPC = Gdx.graphics.getDensity() * 2.54;

  /**
   * Number of pixels on the screen which equals the corresponding length and {@code LengthUnit}.
   * Value is expected in the given regular expression: {@code [0-9]+(px|mm|cm|m)}.
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
  public static long toPixels(@NonNull final String value) {
    if (value == null) {
      throw new IllegalArgumentException("value cannot be null");
    } else if (value.isEmpty()) {
      throw new IllegalArgumentException("value cannot be empty");
    }

    char ch;
    boolean digits = false;
    long sourceLength = 0L;
    changeState: for (int i = 0; i < value.length(); i++) {
      ch = value.charAt(i);
      switch (ch) {
        case '0':case '1':case '2':case '3':case '4':
        case '5':case '6':case '7':case '8':case '9':
          digits = true;
          sourceLength *= 10;
          sourceLength += (ch - 48);
          for (int j = i + 1; j < value.length(); j++) {
            ch = value.charAt(j);
            switch (ch) {
              case '0':case '1':case '2':case '3':case '4':
              case '5':case '6':case '7':case '8':case '9':
                sourceLength *= 10;
                sourceLength += (ch - 48);
                i = j;
                break;
              default:
                continue changeState;
            }
          }
          break;
        case 'c':
          if (!digits) {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException(
                    "expected number followed by either px, mm, cm, or m");
          } else if (i + 1 >= value.length()) {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException("expected m following c, but string ends at c");
          } else if (value.charAt(i + 1) != 'm') {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException("expected m following c");
          }

          assert value.matches("[0-9]+(px|mm|cm|m)")
                  : "value must be given as a number followed by either px, mm, cm, or m";
          return toPixels(sourceLength, CENTIMETERS);
        case 'm':
          if (!digits) {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException(
                    "expected number followed by either px, mm, cm, or m");
          } else if (i + 1 == value.length()) {
            assert value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            return toPixels(sourceLength, METERS);
          } else if (value.charAt(i + 1) != 'm') {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException("expected m following m");
          }

          assert value.matches("[0-9]+(px|mm|cm|m)")
                  : "value must be given as a number followed by either px, mm, cm, or m";
          return toPixels(sourceLength, MILLIMETERS);
        case 'p':
          if (!digits) {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException(
                    "expected number followed by either px, mm, cm, or m");
          } else if (i + 1 >= value.length()) {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException("expected x following p, but string ends at p");
          } else if (value.charAt(i + 1) != 'x') {
            assert !value.matches("[0-9]+(px|mm|cm|m)")
                    : "value must be given as a number followed by either px, mm, cm, or m";
            throw new IllegalArgumentException("expected x following p");
          }

          assert value.matches("[0-9]+(px|mm|cm|m)")
                  : "value must be given as a number followed by either px, mm, cm, or m";
          return sourceLength;
        default:
          assert !value.matches("[0-9]+(px|mm|cm|m)")
                  : "value must be given as a number followed by either px, mm, cm, or m";
          throw new IllegalStateException(
                  "statement has encountered an unexpected character: " + ch);
      }
    }

    assert !value.matches("[0-9]+(px|mm|cm|m)")
            : "value must be given as a number followed by either px, mm, cm, or m";
    throw new IllegalStateException("failed to locate and return source unit");
  }

  /**
   * Number of pixels on the screen which equals the corresponding length and {@code LengthUnit}.
   *
   * @param sourceLength Length of the measurement
   * @param sourceUnit   Unit which the length was given
   *
   * @return Number of pixels corresponding with that length and unit
   */
  public static int toPixels(double sourceLength, LengthUnit sourceUnit) {
    double cms = sourceUnit.toCentimeters(sourceLength);
    return (int)Math.round(PPC * cms);
  }

  /**
   * Scale l by m, checking for overflow. This has a short name to make above code more readable.
   * <p>Note: Taken from {@link java.util.concurrent.TimeUnit#x(long, long, long)}</p>
   */
  private static double x(double l, double m, double over) {
    if (l > over) {
      return Double.MAX_VALUE;
    } else if (l < -over) {
      return Double.MIN_VALUE;
    }

    return l * m;
  }

  /**
   * Equivalent to {@code MILLIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(double, LengthUnit)
   */
  public abstract double toMillimeters(double length);

  /**
   * Equivalent to {@code CENTIMETERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(double, LengthUnit)
   */
  public abstract double toCentimeters(double length);

  /**
   * Equivalent to {@code METERS.convert(length, this)}.
   *
   * @param length length to convert
   * @return converted length, or {@link Long#MIN_VALUE} if conversion would negatively overflow, or
   * {@link Long#MAX_VALUE} if it would positively overflow
   * @see #convert(double, LengthUnit)
   */
  public abstract double toMeters(double length);

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
  public abstract double convert(double sourceLength, @NonNull LengthUnit sourceUnit);

}
