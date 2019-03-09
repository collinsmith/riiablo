package com.riiablo.server;

public class SessionError {

  private int    code;
  private String message;

  private SessionError() {}

  public SessionError(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return code + " " + message;
  }
}
