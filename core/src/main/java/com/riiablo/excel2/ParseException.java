package com.riiablo.excel2;

public class ParseException extends Exception {
  ParseException(String message) {
    super(message);
  }

  ParseException(String format, Object... args) {
    this(String.format(format, args));
  }
}
