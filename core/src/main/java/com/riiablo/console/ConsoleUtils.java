package com.riiablo.console;

import com.google.common.base.Strings;
import java.util.Iterator;

public class ConsoleUtils {
  public static void printList(Console console, Iterable<String> it, int columns, int width) {
    printList(console, it.iterator(), columns, width);
  }

  public static void printList(Console console, Iterator<String> it, int columns, int width) {
    int i = 0;
    StringBuilder sb = new StringBuilder(columns * width);
    while (it.hasNext()) {
      String text = it.next();
      if (columns > 0 && ++i % columns == 0) {
        sb.append(text);
        console.out.println(sb.toString());
        sb.setLength(0);
      } else if (it.hasNext()) {
        String padded = Strings.padEnd(text, width, ' ');
        sb.append(padded);
        if (text.equals(padded)) sb.append(' ');
      } else {
        sb.append(text);
      }
    }

    if (sb.length() > 0) {
      console.out.println(sb.toString());
    }
  }

  private ConsoleUtils() {}
}
