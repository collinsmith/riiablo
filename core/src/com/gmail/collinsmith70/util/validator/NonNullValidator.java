package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.Validator;

public class NonNullValidator<T> implements Validator<T> {

@Override
public boolean isValid(Object obj) {
    if (obj == null) {
        return false;
    }

    return true;
}

}
