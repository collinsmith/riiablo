package com.riiablo.assets;

public class AdapterNotFound extends RuntimeException {
  AdapterNotFound(Class type) {
    this(type, null);
  }

  AdapterNotFound(Class type, Throwable cause) {
    super("Adapter not found for " + type.getSimpleName(), cause);
  }
}
