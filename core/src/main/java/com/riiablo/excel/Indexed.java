package com.riiablo.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags an {@link Excel} to use row indexes as its {@link PrimaryKey}. Used in
 * the case where the excel does not include a primary key column.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {}
