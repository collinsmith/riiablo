package com.gmail.collinsmith70.util;

import com.gmail.collinsmith70.util.validator.AcceptAllValidator;
import com.gmail.collinsmith70.util.validator.NonNullValidator;

public interface Validator<T> {

static Validator ACCEPT_ALL = new AcceptAllValidator();
static Validator ACCEPT_NON_NULL = new NonNullValidator();

boolean isValid(Object obj);

}
