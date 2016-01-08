package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.RangeValueValidator;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * A {@linkplain NumberRangeValueValidator} is a {@link RangeValueValidator} designed to validate
 * generic {@link Number} ranges.
 *
 * @param <T> type of {@link Number} which this {@linkplain NumberRangeValueValidator} validates
 */
public class NumberRangeValueValidator<T extends Number> implements RangeValueValidator<T> {

private static enum NumberComparator implements Comparator<Number> {
    INSTANCE;

    @Override
    public int compare(Number o1, Number o2) {
        return new BigDecimal(o1.toString()).compareTo(new BigDecimal(o2.toString()));
    }

}

private final T MIN;
private final T MAX;

private Boundedness boundedness;

/**
 * Constructs a {@linkplain NumberRangeValueValidator} with a minimum and maximum value.
 *
 * @note min and max should both validate as {@literal true}
 *
 * @param min minimum value
 * @param max maximum value
 */
public NumberRangeValueValidator(T min, T max) {
    if (NumberComparator.INSTANCE.compare(min, max) > 0) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
    this.boundedness = Boundedness.HARD;
}

/**
 * Constructs a {@linkplain NumberRangeValueValidator} intended to validate a single value.
 *
 * @param fixedValue the only value which the {@linkplain NumberRangeValueValidator} accepts
 */
public NumberRangeValueValidator(T fixedValue) {
    this(fixedValue, fixedValue);
}

@Override
public boolean isValid(Number obj) {
    return NumberComparator.INSTANCE.compare(MIN, obj) <= 0
        && NumberComparator.INSTANCE.compare(obj, MAX) <= 0;
}

@Override
public Number getValidatedValue(Number obj) {
    if (boundedness.equals(Boundedness.SOFT)) {
        if (NumberComparator.INSTANCE.compare(obj, MIN) < 0) {
            return MIN;
        } else if (NumberComparator.INSTANCE.compare(MAX, obj) < 0) {
            return MAX;
        }

        return obj;
    }

    if (!isValid(obj)) {
        throw new IllegalArgumentException(String.format(
                "value not within bounds (%s <= value <= %s)",
                MIN,
                MAX));
    }

    return obj;
}

@Override
public T getMin() {
    return MIN;
}

@Override
public T getMax() {
    return MAX;
}

@Override
public void setBoundedness(Boundedness r) {
    this.boundedness = r;
}

@Override
public Boundedness getBoundedness() {
    return boundedness;
}

}
