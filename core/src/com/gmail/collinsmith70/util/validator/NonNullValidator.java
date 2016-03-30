package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidationException;

/**
 * A {@link SimpleValidator} which validates whether or not the passed reference is non-null.
 */
public class NonNullValidator extends SimpleValidator {

  /**
   * Validates that the passed object is not null
   *
   * @param obj object to validate
   */
  @Override
  public void validate(Object obj) {
    if (obj == null) {
      throw new ValidationException("passed reference cannot be null");
    }
  }

}
