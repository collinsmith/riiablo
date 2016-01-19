package com.gmail.collinsmith70.util;

import com.gmail.collinsmith70.util.validator.AcceptAllValidator;
import com.gmail.collinsmith70.util.validator.NonNullNonEmptyStringValidator;
import com.gmail.collinsmith70.util.validator.NonNullValidator;

public interface Validator<T> {

static Validator<?> ACCEPT_ALL = new AcceptAllValidator();
static Validator<?> ACCEPT_NON_NULL = new NonNullValidator();
static Validator<String> ACCEPT_NON_NULL_NON_EMPTY_STRING = new NonNullNonEmptyStringValidator();

boolean isValid(Object obj);

}
