package com.google.collinsmith70.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A field is effectively final if it will never change once set. These fields
 * cannot be created as final because they will not be initialized within a
 * constructor, but are rather "lazily" initialized.
 * 
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
@Documented
@Target(ElementType.FIELD)
public @interface EffectivelyFinal {
}
