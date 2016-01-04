package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.RangeValueValidator;

import java.math.BigDecimal;
import java.util.Comparator;

public class NumberRangeValueValidator implements RangeValueValidator<Number> {

private static enum NumberComparator implements Comparator<Number> {
    INSTANCE;

    @Override
    public int compare(Number o1, Number o2) {
        return new BigDecimal(o1.toString()).compareTo(new BigDecimal(o2.toString()));
    }

}

private final Number MIN;
private final Number MAX;

private Boundedness boundedness;

public NumberRangeValueValidator(Number min, Number max) {
    if (NumberComparator.INSTANCE.compare(min, max) > 0) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
    this.boundedness = Boundedness.HARD;
}

public NumberRangeValueValidator(int fixedValue) {
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
public Number getMin() {
    return MIN;
}

@Override
public Number getMax() {
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
