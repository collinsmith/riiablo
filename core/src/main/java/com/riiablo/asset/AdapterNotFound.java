package com.riiablo.asset;

public class AdapterNotFound extends RuntimeException {
  public final Class type;

  AdapterNotFound(Class type) {
    this(type, null);
  }

  AdapterNotFound(Class type, Throwable cause) {
    super("Adapter not found for " + type.getSimpleName(), cause);
    this.type = type;
  }
}
