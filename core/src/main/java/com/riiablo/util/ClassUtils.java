package com.riiablo.util;

import java.lang.reflect.Field;

import com.badlogic.gdx.utils.Array;

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

  public static Field findField(Class c, Class annotationClass) {
    for (Field f : c.getFields()) {
      if (f.getAnnotation(annotationClass) != null) {
        return f;
      }
    }

    return null;
  }

  public static String toPackagePath(String packageName) {
    return packageName.replace('.', '/');
  }

  public static Class[] findDeclaredClasses(Class c, Class impl) {
    Array<Class> classes = new Array<>(Class.class);
    final Class[] declaredClasses = c.getDeclaredClasses();
    for (Class declaredClass : declaredClasses) {
      if (impl.isAssignableFrom(declaredClass)) {
        classes.add(declaredClass);
      }
    }

    return classes.toArray();
  }
}
