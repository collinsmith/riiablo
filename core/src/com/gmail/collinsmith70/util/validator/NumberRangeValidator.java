package com.gmail.collinsmith70.util.validator;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.util.RangeValidationException;
import com.gmail.collinsmith70.util.RangeValidator;
import com.gmail.collinsmith70.util.ValidationException;
import com.google.common.base.Preconditions;

/**
 * Implementation of a {@link RangeValidator} which validates that objects lie in some
 * {@link Number} range (inclusively).
 *
 * @param <T> {@link Number} subclass to validate
 */
public class NumberRangeValidator<T extends Number & Comparable<? super T>>
        implements RangeValidator<T> {

  /**
   * Minimum value
   */
  private final T MIN;

  /**
   * Maximum value
   */
  private final T MAX;

  /**
   * Constructs a new {@linkplain NumberRangeValidator} instance
   *
   * @param min minimum value
   * @param max maximum value
   */
  public NumberRangeValidator(@NonNull T min, @NonNull T max) {
    this.MIN = Preconditions.checkNotNull(min);
    this.MAX = Preconditions.checkNotNull(max);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getMin() {
    return MIN;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T getMax() {
    return MAX;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

  /**
   * Validates that the passed object is a {@link Number} which lies with the
   * {@linkplain RangeValidator#getMin() minimum} and {@linkplain RangeValidator#getMax() maximum}
   * values of this {@linkplain NumberRangeValidator}
   *
   * @param obj object to validate
   */
  @Override
  public void validate(Object obj) {
    if (obj == null) {
      throw new ValidationException("passed reference cannot be null");
    }

    if (!MIN.getClass().isAssignableFrom(obj.getClass())) {
      throw new ValidationException(
              "passed reference is not a subclass of " + MIN.getClass().getName());
    }

    T castedObj = (T) obj;
    if (MIN.compareTo(castedObj) > 0 && MAX.compareTo(castedObj) < 0) {
      throw new RangeValidationException(MIN, MAX);
    }
  }

}
