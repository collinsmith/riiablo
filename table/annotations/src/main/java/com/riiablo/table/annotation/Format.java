package com.riiablo.table.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field has a custom format when determining matching column
 * names in the parent {@link Schema schema}.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Format {
  /**
   * Start index of {@link #format()} (inclusive).
   */
  int startIndex() default 0;

  /**
   * End index of {@link #format()} (exclusive).
   */
  int endIndex() default 0;

  /**
   * String format of column name, {@code ""} to use field name.
   * <p>
   * Examples:
   * <ul>
   * <li>{@code "class"}
   * <li>{@code "Transform Color"}
   * <li>{@code "Level%s"}
   * <li>{@code "Skill %d"}
   */
  String format() default "";

  /**
   * Index values of format in the case of non-numerical indexes.
   * <p>
   * Examples:
   * <ul>
   * <li>{@code {"", "(N)", "(H)"}}
   * <li>{@code {"r", "g", "b"}}
   * <li>{@code {"Min", "Max", "MagicMin", "MagicMax", "MagicLvl"}}
   */
  String[] values() default {};

  /**
   * Manually sets the column index. This property overrides all other
   * properties. {@code -1} indicates that there is no custom column index.
   */
  int columnIndex() default -1;
}
