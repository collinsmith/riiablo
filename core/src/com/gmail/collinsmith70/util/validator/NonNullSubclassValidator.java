package com.gmail.collinsmith70.util.validator;

import com.google.common.base.Preconditions;

public class NonNullSubclassValidator<T> extends NonNullValidator<T> {

private final Class<T> TYPE;

public NonNullSubclassValidator(Class<T> type) {
    this.TYPE = Preconditions.checkNotNull(type, "Type cannot be null");
}

@Override
public boolean isValid(Object obj) {
    if (!super.isValid(obj)) {
        return false;
    }

    if (!TYPE.isAssignableFrom(obj.getClass())) {
        return false;
    }

    return true;
}

}
