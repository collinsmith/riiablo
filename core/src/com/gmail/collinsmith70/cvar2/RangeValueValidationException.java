package com.gmail.collinsmith70.cvar2;

public class RangeValueValidationException extends ValueValidationException {

public RangeValueValidationException() {
    super();
}

public RangeValueValidationException(String s) {
    super(s);
}

public RangeValueValidationException(Object obj, Object min, Object max) {
    super(String.format(
            "%s not within bounds (%s <= value <= %s)",
            min,
            max));
}

}
