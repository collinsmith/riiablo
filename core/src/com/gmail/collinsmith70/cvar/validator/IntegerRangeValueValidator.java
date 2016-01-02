package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.RangeValueValidator;

public class IntegerRangeValueValidator implements RangeValueValidator<Integer> {

private final int MIN;
private final int MAX;

public IntegerRangeValueValidator(int min, int max) {
    if (max < min) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
}

public IntegerRangeValueValidator(int fixedValue) {
    this(fixedValue, fixedValue);
}

@Override
public boolean isValid(Integer obj) {
    return MIN <= obj && obj <= MAX;
}

@Override
public Integer getMin() {
    return MIN;
}

@Override
public Integer getMax() {
    return MAX;
}

}
