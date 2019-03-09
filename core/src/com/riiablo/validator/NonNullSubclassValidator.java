package com.riiablo.validator;

import android.support.annotation.Nullable;

public class NonNullSubclassValidator<T> extends NonNullValidator {

  private final Class<T> TYPE;

  public NonNullSubclassValidator(Class<T> type) {
    TYPE = type;
  }

  public Class<T> getType() {
    return TYPE;
  }

  @Override
  public void validate(@Nullable Object obj) {
    super.validate(obj);
    if (!TYPE.isAssignableFrom(obj.getClass())) {
      throw new ValidationException(obj.toString() + " is not a subclass of " + TYPE.getName());
    }
  }
}
