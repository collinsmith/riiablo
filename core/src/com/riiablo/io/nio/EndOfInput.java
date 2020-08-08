package com.riiablo.io.nio;

public class EndOfInput extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "The end of the input has been reached!";

  EndOfInput() {
    super(DEFAULT_MESSAGE);
  }

  EndOfInput(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
