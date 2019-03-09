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
      if (++i % columns == 0) {
        sb.append(text);
        console.out.println(sb.toString());
        sb.setLength(0);
      } else if (it.hasNext()) {
        sb.append(Strings.padEnd(text, width, ' '));
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
