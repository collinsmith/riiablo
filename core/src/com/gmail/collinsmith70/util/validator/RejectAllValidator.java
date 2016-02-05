package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidationException;

/**
 * A {@link SimpleValidator} which rejects all passed objects. This class is intended to be used
 * to represent {@linkplain com.gmail.collinsmith70.util.Validator validators} for immutable
 * structures.
 */
public final class RejectAllValidator extends SimpleValidator {

/**
 * Rejects the passed object and throws a {@link ValidationException}
 *
 * @param obj object to validate
 */
@Override
public void validate(Object obj) {
    throw new ValidationException("this validator is immutable and rejects all objects");
}

}
