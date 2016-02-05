package com.gmail.collinsmith70.util;

/**
 * Functional interface declaring that a class' instances should be {@link Validatable} and require
 * the {@link #isValid} method to determine whether or not certain values are correct in "setter"
 * methods.
 *
 * <p>
 * For example:
 * </p>
 * <pre>
 * {@code
 * class Name implements Validatable<String> {
 *     private String firstName;
 *
 *     boolean isValid(Object obj) {
 *         if (obj == null) {
 *             return false;
 *         } else if (!(obj instanceof String)) {
 *             return false;
 *         }
 *
 *         return !((String)obj).isEmpty();
 *     }
 *
 *     void setFirstName(String firstName) {
 *         if (!isValid(firstName)) {
 *             throw new IllegalArgumentException("First names must be non-null non-empty Strings");
 *         }
 *
 *         this.firstName = firstName;
 *     }
 * }
 * }
 * </pre>
 */
@FunctionalInterface
public interface Validatable {

/**
 * @param obj object to validate
 *
 * @return {@code true} if the object is valid, otherwise {@code false}
 */
boolean isValid(Object obj);

}
