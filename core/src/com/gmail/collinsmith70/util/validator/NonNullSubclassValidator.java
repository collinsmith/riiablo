package com.gmail.collinsmith70.util.validator;

import com.gmail.collinsmith70.util.ValidationException;
import com.google.common.base.Preconditions;

/**
 * A {@link NonNullValidator} which validates that the passed value is both non-null and a subclass
 * of {@link T}
 *
 * @param <T> type of object which this {@linkplain NonNullSubclassValidator} is specifically
 *            designed to validate as a subclass of
 */
public class NonNullSubclassValidator<T> extends NonNullValidator {

  /**
   * Reference the {@linkplain Class} this {@linkplain NonNullSubclassValidator} is basing its
   * {@linkplain #validate(Object) validations} on
   */
  private final Class<T> TYPE;

  /**
   * Constructs a new {@linkplain NonNullSubclassValidator} instance.
   *
   * @param type {@linkplain Class} reference to check if passed objects are subclasses of
   */
  public NonNullSubclassValidator(Class<T> type) {
    this.TYPE = Preconditions.checkNotNull(type, "Type cannot be null");
  }

  /**
   * @return {@linkplain Class} reference to check if passed objects are subclasses of
   */
  public Class<T> getType() {
    return TYPE;
  }

  /**
   * Validates that the passed value is a subclass of the {@linkplain #getType() type} this instance
   * represents.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  public void validate(Object obj) {
    super.validate(obj);
    if (!TYPE.isAssignableFrom(obj.getClass())) {
      throw new ValidationException("passed reference is not a subclass of " + TYPE.getName());
    }
  }

}
