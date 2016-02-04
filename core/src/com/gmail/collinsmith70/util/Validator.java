package com.gmail.collinsmith70.util;

import com.gmail.collinsmith70.util.validator.AcceptAllValidator;
import com.gmail.collinsmith70.util.validator.NonNullNonEmptyStringValidator;
import com.gmail.collinsmith70.util.validator.NonNullValidator;
import com.gmail.collinsmith70.util.validator.RejectAllValidator;

/**
 * Abstract representation of a validator which checks the
 * <a href="https://en.wikipedia.org/wiki/Validity">validity</a> of arguments.
 *
 * @param <T> type of object which this {@linkplain Validator} is specifically designed to validate
 */
public interface Validator<T> extends Validatable<T> {

/**
 * Constant reference to a {@linkplain Validator} which accepts all values
 */
static Validator<?> ACCEPT_ALL = new AcceptAllValidator();

/**
 * Constant reference to a {@linkplain Validator} which rejects all values
 */
static Validator<?> REJECT_ALL = new RejectAllValidator();

/**
 * Constant reference to a {@linkplain Validator} which accepts all non-null values
 */
static Validator<?> ACCEPT_NON_NULL = new NonNullValidator();

/**
 * Constant reference to a {@linkplain Validator} which accepts all {@linkplain String}s which are
 * both non-null and {@linkplain String#isEmpty() non-empty}
 */
static Validator<String> ACCEPT_NON_NULL_NON_EMPTY_STRING = new NonNullNonEmptyStringValidator();

/**
 * Validates the specified object and throws a {@link ValidationException} with a reason if it is
 * not valid.
 *
 * @param obj object to validate
 *
 * @throws ValidationException if the passed object is invalid
 */
void validate(Object obj);

}
