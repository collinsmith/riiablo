package com.gmail.collinsmith70.cvar.validator;

import com.gmail.collinsmith70.cvar.ValueValidator;

public enum NullValueValidator implements ValueValidator {
INSTANCE;

@Override
public boolean isValid(Object obj) {
    return true;
}

}
