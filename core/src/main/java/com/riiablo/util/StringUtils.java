package com.riiablo.util;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  private StringUtils() {}

  private static final Pattern PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  @NonNull
  public static String[] parseArgs(@NonNull CharSequence buffer) {
    String tmp;
    Collection<String> args = new ArrayList<>(8);
    Matcher matcher = PATTERN.matcher(buffer);
    while (matcher.find()) {
      if ((tmp = matcher.group(1)) != null) {
        // Add double-quoted string without the quotes
        args.add(tmp);
      } else if ((tmp = matcher.group(2)) != null) {
        // Add single-quoted string without the quotes
        args.add(tmp);
      } else {
        // Add unquoted word
        args.add(matcher.group());
      }
    }

    return args.toArray(new String[args.size()]);
  }

  @NonNull
  public static String commonPrefix(@NonNull Iterable<String> it) {
    return commonPrefix(it.iterator());
  }

  @NonNull
  public static String commonPrefix(@NonNull Iterator<String> it) {
    String commonPrefix = null;
    while (it.hasNext()) {
      if (commonPrefix == null) {
        commonPrefix = it.next();
      } else if (commonPrefix.isEmpty()) {
        break;
      } else {
        commonPrefix = org.apache.commons.lang3.StringUtils
            .getCommonPrefix(commonPrefix, it.next());
      }
    }

    return org.apache.commons.lang3.StringUtils
        .defaultString(commonPrefix);
  }
}
