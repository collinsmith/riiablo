package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidatorException;

public class NonNullValidator<T> extends SimpleValidator<T> {

@Override
public void validate(Object obj) {
    if (obj == null) {
        throw new ValidatorException("passed reference cannot be null");
    }
}

}
