package com.riiablo.io;

public class EndOfInput extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "The end of the input has been reached!";

  public EndOfInput() {
    super(DEFAULT_MESSAGE);
  }

  public EndOfInput(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
