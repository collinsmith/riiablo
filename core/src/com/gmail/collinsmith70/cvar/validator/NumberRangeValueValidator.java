package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.RangeValueValidator;

import java.math.BigDecimal;
import java.util.Comparator;

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

public NumberRangeValueValidator(T min, T max) {
    if (NumberComparator.INSTANCE.compare(min, max) > 0) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
    this.boundedness = Boundedness.HARD;
}

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
