package com.riiablo.asset;

public class ParamsNotFound extends RuntimeException {
  public final Class type;

  public ParamsNotFound(Class type) {
    this(type, null);
  }

  public ParamsNotFound(Class type, Throwable cause) {
    super("Default params not found for " + type.getSimpleName(), cause);
    this.type = type;
  }
}
