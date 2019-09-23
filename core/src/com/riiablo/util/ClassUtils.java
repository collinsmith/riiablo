package com.riiablo.util;

import java.lang.reflect.Field;

public class ClassUtils {
  private ClassUtils() {}

  public static boolean isDeclaredField(Class c, Field f) {
    try {
      return c.getDeclaredField(f.getName()) != null;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  public static boolean hasAnnotation(Class c, Class annotationClass) {
    return c.getAnnotation(annotationClass) != null;
  }
}
