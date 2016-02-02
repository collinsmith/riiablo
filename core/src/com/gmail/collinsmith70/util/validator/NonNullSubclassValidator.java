package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidatorException;
import com.google.common.base.Preconditions;

public class NonNullSubclassValidator<T> extends NonNullValidator<T> {

private final Class<T> TYPE;

public NonNullSubclassValidator(Class<T> type) {
    this.TYPE = Preconditions.checkNotNull(type, "Type cannot be null");
}

@Override
public void validate(Object obj) {
    super.validate(obj);
    if (!TYPE.isAssignableFrom(obj.getClass())) {
        throw new ValidatorException("passed reference is not a subclass of " + TYPE.getName());
    }
}

}
