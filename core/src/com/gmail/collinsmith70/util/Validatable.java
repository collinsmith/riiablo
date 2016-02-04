package com.gmail.collinsmith70.util;

public interface Validatable<T> {

/**
 * @param obj object to validate
 *
 * @return {@code true} if the object is valid, otherwise {@code false}
 */
boolean isValid(Object obj);

}
