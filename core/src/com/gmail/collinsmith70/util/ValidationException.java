package com.gmail.collinsmith70.util;

/**
 * A {@link RuntimeException} which is thrown when {@link Validator#validate(Object)} invalidates
 * a specified object.
 */
public class ValidationException extends RuntimeException {

  /**
   * Constructs a new {@linkplain ValidationException}.
   *
   * @param reason brief reason on why the object was invalidated
   */
  public ValidationException(String reason) {
    super(reason);
  }

}
