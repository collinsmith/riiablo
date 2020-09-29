package com.riiablo.assets;

public class ReaderNotFound extends RuntimeException {
  ReaderNotFound(Class type) {
    this(type, null);
  }

  ReaderNotFound(Class type, Throwable cause) {
    super("Reader not found for " + type.getSimpleName(), cause);
  }
}
