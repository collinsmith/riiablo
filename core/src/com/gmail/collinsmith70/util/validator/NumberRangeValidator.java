package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.RangeValidator;

public class NumberRangeValidator<T extends Number & Comparable<? super T>>
        implements RangeValidator<T> {

private final T MIN;
private final T MAX;

public NumberRangeValidator(T min, T max) {
    this.MIN = min;
    this.MAX = max;
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
public boolean isValid(Object obj) {
    if (obj == null) {
        return false;
    }

    if (!MIN.getClass().isAssignableFrom(obj.getClass())) {
        return false;
    }

    T castedObj = (T)obj;
    return MIN.compareTo(castedObj) <= 0
        && MAX.compareTo(castedObj) >= 0;
}

}
