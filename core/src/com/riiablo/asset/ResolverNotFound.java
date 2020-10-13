package com.riiablo.asset;

import io.netty.util.AsciiString;

public class ResolverNotFound extends RuntimeException {
  final AsciiString path;

  ResolverNotFound(AsciiString path) {
    this(path, null);
  }

  ResolverNotFound(AsciiString path, Throwable cause) {
    super("Resolver not found for " + path, cause);
    this.path = path;
  }

  public AsciiString path() {
    return path;
  }
}
