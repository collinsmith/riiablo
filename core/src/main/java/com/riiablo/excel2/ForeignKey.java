package com.riiablo.excel2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags an {@link Excel.Entry} field as a reference to another entry in the
 * specified {@link Excel} class.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
  Class<? extends Excel> value();
}
