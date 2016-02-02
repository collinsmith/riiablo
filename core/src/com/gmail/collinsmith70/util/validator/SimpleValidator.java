package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.ValidatorException;

public abstract class SimpleValidator<T> implements Validator<T> {

@Override
public boolean isValid(Object obj) {
    try {
        validate(obj);
        return true;
    } catch (ValidatorException e) {
        return false;
    }
}

}
