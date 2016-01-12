package com.gmail.collinsmith70.cvar;

/**
 * Interface for representing methods required by a ValueValidator. A ValueValidator should be able
 * to validate arbitrary values ({@link #isValid(Object)}) as well as retrieve a (possibly modified)
 * validated value ({@link #getValidatedValue(Object)}).
 *
 * @param <T> type of the object to be validated
 */
public interface ValueValidator<T> {

/**
 * Checkes whether or not a value is valid according to the constraints of the implementation.
 *
 * @param obj object to validate
 *
 * @return
 */
boolean isValid(T obj);

/**
 * Checks and adjusts a given value so that it is valid. Typically some implementations will return
 * the passed object reference, unmodified, while others may adjust it so that it is considered
 * valid.
 *
 * @param obj object to check and possibly adjust
 *
 * @return the adjusted value, otherwise the passed object reference if no adjustments are necessary
 *
 * @throws IllegalArgumentException when the implementation rejects the passed object (i.e., it is
 *                                  considered invalid and the implementation does not perform
 *                                  adjustments
 */
T getValidatedValue(T obj);

/**
 * Validates that a specified value is acceptable by the implementation and throws a
 * {@link ValueValidationException} if it is not
 * 
 * @throws ValueValidationException when the passed object is not accepted by the implementation
 */
void validate(T obj) throws ValueValidationException;

}
