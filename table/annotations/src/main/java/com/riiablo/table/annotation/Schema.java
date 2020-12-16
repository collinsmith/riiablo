package com.riiablo.table.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.riiablo.table.Table;

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

  /**
   * Number of expected records of this type.
   *
   * @see #loadFactor()
   * @see <a href="https://planetmath.org/goodhashtableprimes">good hash table primes</a>
   */
  int initialCapacity() default Table.DEFAULT_INITIAL_CAPACITY;

  /**
   * Percentage factor when the table should be resized.
   *
   * @see #initialCapacity()
   */
  float loadFactor() default Table.DEFAULT_LOAD_FACTOR;
}
