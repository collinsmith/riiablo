package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.RangeValueValidator;

public class IntegerRangeValueValidator implements RangeValueValidator<Integer> {

private final int MIN;
private final int MAX;

private boolean softlyBound;

public IntegerRangeValueValidator(int min, int max) {
    if (max < min) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
    this.softlyBound = false;
}

public IntegerRangeValueValidator(int fixedValue) {
    this(fixedValue, fixedValue);
}

@Override
public boolean isValid(Integer obj) {
    return MIN <= obj && obj <= MAX;
}

@Override
public Integer getValidatedValue(Integer obj) {
    if (softlyBound) {
        if (obj < MIN) {
            return MIN;
        } else if (obj > MAX) {
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
public Integer getMin() {
    return MIN;
}

@Override
public Integer getMax() {
    return MAX;
}

@Override
public void setSoftlyBound(boolean b) {
    this.softlyBound = b;
}

@Override
public boolean isSoftlyBound() {
    return softlyBound;
}

}
