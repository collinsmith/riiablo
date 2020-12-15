package com.riiablo.table.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the specified type is a record schema.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Schema {
  /**
   * Offset of the first record.
   */
  int offset() default 0;

  /**
   * Whether or not records are indexed in their natural ordering. Setting this
   * will override any set {@link PrimaryKey primary key}.
   */
  boolean indexed() default false;
}
