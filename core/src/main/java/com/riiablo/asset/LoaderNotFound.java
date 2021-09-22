package com.riiablo.asset;

public class LoaderNotFound extends RuntimeException {
  public final Class type;

  LoaderNotFound(Class type) {
    this(type, null);
  }

  LoaderNotFound(Class type, Throwable cause) {
    super("Loader not found for " + type.getSimpleName(), cause);
    this.type = type;
  }
}
