package com.riiablo.mpq_bytebuf;

public class InvalidFormat extends RuntimeException {
  public InvalidFormat(String msg) {
    super(msg);
  }

  public InvalidFormat(String msg, Throwable cause) {
    super(msg, cause);
  }
}
