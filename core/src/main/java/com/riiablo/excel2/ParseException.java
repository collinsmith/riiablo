package com.riiablo.excel2;

import java.lang.reflect.Field;

import com.badlogic.gdx.utils.Array;

public class ParseException extends Exception {
  ParseException(String message) {
    super(message);
  }

  ParseException(String format, Object... args) {
    this(String.format(format, args));
  }

  ParseException(Field field, String format, Object... args) {
    this(format, args);

    // Formats the leading stack trace element like:
    //   at com.riiablo.excel.txt.MonStats$Entry.hcIdx2(MonStats.java:0)
    Class declaringClass = field.getDeclaringClass();
    StackTraceElement fieldElement = new StackTraceElement(
        declaringClass.getName(),
        field.getName(),
        getRootClass(declaringClass).getSimpleName() + ".java",
        0); // 0 indicates line 0 -- non-zero required for link parsing in IDEA

    StackTraceElement[] originalStackTrace = getStackTrace();
    Array<StackTraceElement> stackTrace = new Array<>(
        true,
        originalStackTrace.length + 1,
        StackTraceElement.class);
    stackTrace.add(fieldElement);
    stackTrace.addAll(originalStackTrace);
    setStackTrace(stackTrace.toArray());
  }

  ParseException(Class clazz, String format, Object... args) {
    this(format, args);

    // Formats the leading stack trace element like:
    //   at com.riiablo.excel.txt.MonStats$Entry.hcIdx2(MonStats.java:0)
    Class declaringClass = clazz.getDeclaringClass();
    StackTraceElement fieldElement = new StackTraceElement(
        declaringClass.getName(),
        clazz.getName(),
        getRootClass(declaringClass).getSimpleName() + ".java",
        0); // 0 indicates line 0 -- non-zero required for link parsing in IDEA

    StackTraceElement[] originalStackTrace = getStackTrace();
    Array<StackTraceElement> stackTrace = new Array<>(
        true,
        originalStackTrace.length + 1,
        StackTraceElement.class);
    stackTrace.add(fieldElement);
    stackTrace.addAll(originalStackTrace);
    setStackTrace(stackTrace.toArray());
  }

  private static Class getRootClass(Class c) {
    Class declaringClass = c;
    for (
        Class parent = declaringClass;
        (parent = parent.getDeclaringClass()) != null;) {
      declaringClass = parent;
    }

    return declaringClass;
  }
}
