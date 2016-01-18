package com.gmail.collinsmith70.util.validator;

public class NonNullNonEmptyStringValidator extends NonNullValidator<String> {

@Override
public boolean isValid(Object obj) {
    if (!super.isValid(obj)) {
        return false;
    }

    if (!(obj instanceof String)) {
        return false;
    }

    String str = (String)obj;
    return !str.isEmpty();
}

}
