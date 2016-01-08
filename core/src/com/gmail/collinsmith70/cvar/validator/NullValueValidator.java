package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.ValueValidator;

/**
 * A {@linkplain NullValueValidator} is a {@link ValueValidator} which validates all values as
 * {@literal true}.
 */
public enum NullValueValidator implements ValueValidator {
/**
 * @see NullValueValidator
 */
INSTANCE;

@Override
public boolean isValid(Object obj) {
    return true;
}

@Override
public Object getValidatedValue(Object obj) {
    return obj;
}

}
