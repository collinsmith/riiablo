package com.gmail.collinsmith70.util;

public class RangeValidatorException extends ValidatorException {

public RangeValidatorException(String reason) {
    super(reason);
}

public RangeValidatorException(Object min, Object max) {
    super(String.format("passed reference must lie between %s and %s (inclusive)", min, max));
}

}
