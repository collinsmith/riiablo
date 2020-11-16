package com.riiablo.asset;

public class LoaderNotFound extends RuntimeException {
  LoaderNotFound(Class type) {
    this(type, null);
  }

  LoaderNotFound(Class type, Throwable cause) {
    super("Loader not found for " + type.getSimpleName(), cause);
  }
}
