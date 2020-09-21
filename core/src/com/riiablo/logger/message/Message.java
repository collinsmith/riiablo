package com.riiablo.logger.message;

public interface Message {
  void release();
  String format();
  String pattern();
  Object[] parameters();
  int numParameters();
  Throwable throwable();
}
