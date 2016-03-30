package com.gmail.collinsmith70.util;

/**
 * A {@link Validator} which is designed to validate that an object lies between some arbitrary
 * {@linkplain #getMin() minimum} and {@linkplain #getMax() maximum}
 *
 * @param <T> type of object which this {@linkplain RangeValidator} is specifically designed to
 *            validate
 */
public interface RangeValidator<T extends Comparable<? super T>> extends Validator {

  /**
   * @return minimum of the {@link RangeValidator}
   */
  T getMin();

  /**
   * @return maximum of the {@link RangeValidator}
   */
  T getMax();

}
