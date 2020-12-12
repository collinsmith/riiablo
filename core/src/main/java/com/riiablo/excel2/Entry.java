package com.riiablo.excel2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags this class as an {@link Excel} class and defines the
 * {@link Excel.Entry} row schema.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry {
  Class<? extends Excel.Entry> value();
}
