package com.riiablo.asset;

public class ResolverNotFound extends RuntimeException {
  final CharSequence path;

  ResolverNotFound(CharSequence path) {
    this(path, null);
  }

  ResolverNotFound(CharSequence path, Throwable cause) {
    super("Resolver not found for " + path, cause);
    this.path = path;
  }

  public CharSequence path() {
    return path;
  }
}
