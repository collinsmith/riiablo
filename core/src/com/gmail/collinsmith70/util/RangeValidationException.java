package com.gmail.collinsmith70.util;

import com.google.common.base.Preconditions;
import com.sun.istack.internal.NotNull;

/**
 * A {@link ValidationException} which is thrown when a {@link RangeValidator} invalidates that an
 * object is outside of the specified range.
 */
public class RangeValidationException extends ValidationException {

/**
 * Reference to the {@linkplain RangeValidator#getMin() minimum} acceptable value
 */
private final Object MIN;

/**
 * Reference to the {@linkplain RangeValidator#getMax() maximum} acceptable value
 */
private final Object MAX;

/**
 * Constructs a new {@link RangeValidationException} instance with a {@code null} minimum and
 * maximum. The reason should reflect how the value was outside of the acceptable range.
 *
 * @param reason brief reason why the value was invalidated
 */
public RangeValidationException(String reason) {
    super(reason);
    this.MIN = null;
    this.MAX = null;
}

/**
 * Constructs a new {@link RangeValidationException} instance
 *
 * @param min arbitrary minimum of the {@link RangeValidator}
 * @param max arbitrary maximum of the {@link RangeValidator}
 */
public RangeValidationException(@NotNull Object min, @NotNull Object max) {
    super(String.format("passed reference must lie between %s and %s (inclusive)", min, max));
    this.MIN = Preconditions.checkNotNull(min);
    this.MAX = Preconditions.checkNotNull(max);
}

/**
 * @return minimum of the {@link RangeValidator}
 */
public Object getMin() {
    return MIN;
}

/**
 * @return maximum of the {@link RangeValidator}
 */
public Object getMax() {
    return MAX;
}

}
