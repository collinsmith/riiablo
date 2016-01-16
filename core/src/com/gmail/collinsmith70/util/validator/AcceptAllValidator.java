package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.Validator;

public class AcceptAllValidator<T> implements Validator<T> {

@Override
public boolean isValid(Object obj) {
    return true;
}

}
