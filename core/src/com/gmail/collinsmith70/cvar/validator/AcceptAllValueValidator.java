package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.ValueValidationException;
import com.gmail.collinsmith70.cvar.ValueValidator;

/**
 * A {@linkplain AcceptAllValueValidator} is a {@link ValueValidator} which validates all values as
 * {@literal true}.
 */
public enum AcceptAllValueValidator implements ValueValidator {
/**
 * @see AcceptAllValueValidator
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

@Override
public void validate(Object obj) throws ValueValidationException {
    //...
}

}
