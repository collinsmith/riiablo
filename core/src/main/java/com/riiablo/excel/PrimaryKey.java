package com.riiablo.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags an {@link Excel.Entry} field as the primary way to reference itself.
 * Field must be an {@code int}, {@link Integer}, or {@link String}.
 *
 * @see #SUPPORTED_TYPES
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
  Class[] SUPPORTED_TYPES = { int.class, Integer.class, String.class };
}
