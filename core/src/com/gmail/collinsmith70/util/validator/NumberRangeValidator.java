package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.RangeValidator;
import com.gmail.collinsmith70.util.RangeValidatorException;
import com.gmail.collinsmith70.util.ValidatorException;

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
    try {
        validate(obj);
        return true;
    } catch (ValidatorException e) {
        return false;
    }
}

@Override
public void validate(Object obj) {
    if (obj == null) {
        throw new ValidatorException("passed reference cannot be null");
    }

    if (!MIN.getClass().isAssignableFrom(obj.getClass())) {
        throw new ValidatorException(
                "passed reference is not a subclass of " + MIN.getClass().getName());
    }

    T castedObj = (T)obj;
    if (MIN.compareTo(castedObj) > 0 && MAX.compareTo(castedObj) < 0) {
        throw new RangeValidatorException(MIN, MAX);
    }
}

}
