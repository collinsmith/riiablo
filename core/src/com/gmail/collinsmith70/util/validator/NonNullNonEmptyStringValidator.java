package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidatorException;

public class NonNullNonEmptyStringValidator extends NonNullValidator<String> {

@Override
public void validate(Object obj) {
    super.validate(obj);
    if (!(obj instanceof String)) {
        throw new ValidatorException("passed reference is not a String");
    }

    String str = (String)obj;
    if (str.isEmpty()) {
        throw new ValidatorException("passed String is empty");
    }
}

}
