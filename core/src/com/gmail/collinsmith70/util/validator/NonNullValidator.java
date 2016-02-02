package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidationException;

/**
 * A {@link SimpleValidator} which validates whether or not the passed reference is non-null.
 *
 * @param <T> type of object which this {@linkplain NonNullValidator} is specifically designed
 *            to validate
 */
public class NonNullValidator<T> extends SimpleValidator<T> {

/**
 * Validates that the passed object is not null
 *
 * @param obj object to validate
 */
@Override
public void validate(Object obj) {
    if (obj == null) {
        throw new ValidationException("passed reference cannot be null");
    }
}

}
