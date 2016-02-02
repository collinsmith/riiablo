package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidatorException;

public class RejectAllValidator<T> extends SimpleValidator<T> {

@Override
public void validate(Object obj) {
    throw new ValidatorException("this validator is immutable and rejects all objects");
}

}
