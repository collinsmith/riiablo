package com.gmail.collinsmith70.cvar.validator;

public class PositiveNumberRangeValueValidator extends NumberRangeValueValidator {

public PositiveNumberRangeValueValidator(Number max) {
    super(0, max);
}

}
