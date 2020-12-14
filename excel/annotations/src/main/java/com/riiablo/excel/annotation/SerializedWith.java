package com.riiablo.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the specified {@link Schema schema} should use the given
 * {@link #value() serializer} in lieu of generating one. The serializer
 * implementation should have the schema set as its generic parameter.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SerializedWith {
  /**
   * A serializer implementation that should be used by this
   * {@link Schema schema} in lieu of generating one.
   */
  Class<? extends com.riiablo.excel.Serializer<?>> value();
}
