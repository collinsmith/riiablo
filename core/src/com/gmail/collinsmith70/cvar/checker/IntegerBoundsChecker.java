package com.gmail.collinsmith70.cvar.checker;

import com.gmail.collinsmith70.cvar.BoundedRangeChecker;

public class IntegerBoundsChecker implements BoundedRangeChecker<Integer> {

private final int MIN;
private final int MAX;

public IntegerBoundsChecker(int min, int max) {
    if (max < min) {
        throw new IllegalArgumentException("min should be <= max");
    }

    this.MIN = min;
    this.MAX = max;
}

public IntegerBoundsChecker(int fixedValue) {
    this(fixedValue, fixedValue);
}

@Override
public boolean isWithinBounds(Integer obj) {
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
