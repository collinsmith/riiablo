package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.Validator;
import com.gmail.collinsmith70.util.ValidationException;

/**
 * Abstract partial implementation of a {@link Validator} which determines the return value of
 * {@link #isValid(Object)} by checking whether or not {@link #validate(Object)} threw an
 * exception.
 */
public abstract class SimpleValidator implements Validator {

  /**
   * @param obj object to validate
   * @return {@code true} if {@link #validate(Object)} will not throw an exception,
   * otherwise {@code false}
   */
  @Override
  public boolean isValid(Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

}
