package com.riiablo.util;

import java.lang.annotation.Annotation;
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

  @SuppressWarnings("unchecked")
  public static boolean hasAnnotation(Class c, Class<? extends Annotation> annotationClass) {
    return c.getAnnotation(annotationClass) != null;
  }

  public static Field findField(Class c, Class<? extends Annotation> annotationClass) {
    for (Field f : c.getFields()) {
      if (f.getAnnotation(annotationClass) != null) {
        return f;
      }
    }

    return null;
  }
}
