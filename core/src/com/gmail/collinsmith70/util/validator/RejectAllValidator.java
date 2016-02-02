package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidationException;

/**
 * A {@link SimpleValidator} which rejects all passed objects. This class is intended to be used
 * to represent {@linkplain com.gmail.collinsmith70.util.Validator validators} for immutable
 * structures.
 *
 * @param <T> type of object which this {@linkplain RejectAllValidator} is specifically designed
 *            to validate
 */
public final class RejectAllValidator<T> extends SimpleValidator<T> {

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
