package com.gmail.collinsmith70.util;

public class RangeValidatorException extends ValidatorException {

private final Object MIN;
private final Object MAX;

public RangeValidatorException(String reason) {
    super(reason);
    this.MIN = null;
    this.MAX = null;
}

public RangeValidatorException(Object min, Object max) {
    super(String.format("passed reference must lie between %s and %s (inclusive)", min, max));
    this.MIN = min;
    this.MAX = max;
}

public Object getMin() {
    return MIN;
}

public Object getMax() {
    return MAX;
}

}
