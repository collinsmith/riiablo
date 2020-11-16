package com.riiablo.logger.message;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;

public class ParameterFormatter {
    static final String RECURSION_PREFIX = "[...";
    static final String RECURSION_SUFFIX = "...]";
    static final String ERROR_PREFIX = "[!!!";
    static final String ERROR_SEPARATOR = "=>";
    static final String ERROR_MSG_SEPARATOR = ":";
    static final String ERROR_SUFFIX = "!!!]";

    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';

  private ParameterFormatter() {}

  static int countArgs(final String messagePattern, final int[] offsets) {
    if (messagePattern == null) return 0;
    final int length = messagePattern.length();
    int result = 0;
    boolean escape = false;
    for (int i = 0; i < length - 1; i++) {
      final char curChar = messagePattern.charAt(i);
      switch (curChar) {
        case ESCAPE_CHAR:
          escape = !escape;
          offsets[0] = -1; // escaping means fast path is not available...
          result++;
          break;
        case DELIM_START:
          if (!escape && messagePattern.charAt(i + 1) == DELIM_STOP) {
            offsets[result] = i;
            result++;
            i++;
          }
          escape = false;
          break;
        default:
          escape = false;
      }
    }
    return result;
  }

  static StringBuilder format(
      final StringBuilder buffer,
      final String messagePattern,
      final Object[] args,
      final int argc,
      final int[] offsets) {
    if (messagePattern == null || args == null || argc == 0) {
      return buffer.append(messagePattern);
    }

    int previous = 0;
    for (int i = 0; i < argc; i++) {
      buffer.append(messagePattern, previous, offsets[i]);
      previous = offsets[i] + 2;
      recursiveDeepToString(buffer, args[i]);
    }

    return buffer.append(messagePattern, previous, messagePattern.length());
  }

  static void recursiveDeepToString(
      final StringBuilder buffer,
      final Object o) {
    if (appendSpecificTypes(buffer, o)) return;
    if (isMaybeRecursive(o)) {
      appendPotentiallyRecursiveValue(buffer, o);
    } else {
      tryObjectToString(buffer, o);
    }
  }

  static boolean appendSpecificTypes(final StringBuilder buffer, final Object obj) {
    if (obj == null || obj instanceof String) {
      buffer.append((String) obj);
    } else if (obj instanceof CharSequence) {
      buffer.append((CharSequence) obj);
    } else if (obj instanceof Integer) {
      buffer.append(((Integer) obj).intValue());
    } else if (obj instanceof Long) {
      buffer.append(((Long) obj).longValue());
    } else if (obj instanceof Double) {
      buffer.append(((Double) obj).doubleValue());
    } else if (obj instanceof Boolean) {
      buffer.append(((Boolean) obj).booleanValue());
    } else if (obj instanceof Character) {
      buffer.append(((Character) obj).charValue());
    } else if (obj instanceof Short) {
      buffer.append(((Short) obj).shortValue());
    } else if (obj instanceof Float) {
      buffer.append(((Float) obj).floatValue());
    } else if (obj instanceof Byte) {
      buffer.append(((Byte) obj).byteValue());
    } else {
      return false;
    }
    return true;
  }

  static boolean isMaybeRecursive(final Object o) {
    return o.getClass().isArray() || o instanceof Map || o instanceof Collection;
  }

  static void appendPotentiallyRecursiveValue(
      final StringBuilder buffer, final Object o) {
    final Class<?> clazz = o.getClass();
    if (clazz.isArray()) {
      appendArray(buffer, o, clazz);
    } else {
      // TODO: support collections
      tryObjectToString(buffer, o);
    }
  }

  static void appendArray(
      final StringBuilder buffer, final Object o, final Class<?> clazz) {
    if (clazz == byte[].class) {
      buffer.append(Arrays.toString((byte[]) o));
    } else if (clazz == short[].class) {
      buffer.append(Arrays.toString((short[]) o));
    } else if (clazz == int[].class) {
      buffer.append(Arrays.toString((int[]) o));
    } else if (clazz == long[].class) {
      buffer.append(Arrays.toString((long[]) o));
    } else if (clazz == float[].class) {
      buffer.append(Arrays.toString((float[]) o));
    } else if (clazz == double[].class) {
      buffer.append(Arrays.toString((double[]) o));
    } else if (clazz == boolean[].class) {
      buffer.append(Arrays.toString((boolean[]) o));
    } else if (clazz == char[].class) {
      buffer.append(Arrays.toString((char[]) o));
    } else {
      /** TODO: support {@link #recursiveDeepToString} */
      buffer.append(Arrays.deepToString((Object[]) o));
    }
  }

  static void tryObjectToString(final StringBuilder buffer, final Object o) {
    // it's just some other Object, we can only use toString().
    try {
      buffer.append(o.toString());
    } catch (final Throwable t) {
      handleErrorInObjectToString(buffer, o, t);
    }
  }

  private static void handleErrorInObjectToString(
      final StringBuilder buffer, final Object o, final Throwable t) {
    buffer.append(ERROR_PREFIX);
    buffer.append(ObjectUtils.identityToString(o));
    buffer.append(ERROR_SEPARATOR);
    final String msg = t.getMessage();
    final String className = t.getClass().getName();
    buffer.append(className);
    if (!className.equals(msg)) {
      buffer.append(ERROR_MSG_SEPARATOR);
      buffer.append(msg);
    }
    buffer.append(ERROR_SUFFIX);
  }
}
