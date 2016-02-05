package com.gmail.collinsmith70.util.validator;

/**
 * A {@link SimpleValidator} which accepts all passed references. This class is intended to be used
 * to represent {@linkplain com.gmail.collinsmith70.util.Validator validators} for structures where
 * the value does not require validation.
 */
public final class AcceptAllValidator extends SimpleValidator {

/**
 * Validates the passed object
 *
 * @param obj object to validate
 */
@Override
public void validate(Object obj) {}

}
