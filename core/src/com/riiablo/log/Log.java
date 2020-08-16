package com.riiablo.log;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

public class Log {
  private static final MessageFactory FORMAT_MESSAGE_FACTORY = StringFormatterMessageFactory.INSTANCE;

  private static Message createMessage(String format, Object... args) {
    return FORMAT_MESSAGE_FACTORY.newMessage(format, args);
  }

  public static void tracef(Logger logger, String format, Object... args) {
    logger.trace(createMessage(format, args));
  }

  public static void debugf(Logger logger, String format, Object... args) {
    logger.debug(createMessage(format, args));
  }

  public static void infof(Logger logger, String format, Object... args) {
    logger.info(createMessage(format, args));
  }

  public static void warnf(Logger logger, String format, Object... args) {
    logger.warn(createMessage(format, args));
  }

  public static void warnf(Logger logger, Throwable t, String format, Object... args) {
    logger.warn(createMessage(format, args), t);
  }

  public static void errorf(Logger logger, Throwable t, String format, Object... args) {
    logger.error(createMessage(format, args), t);
  }

  public static void put(String key, String value) {
    ThreadContext.put(key, value);
  }

  public static void put(String key, int value) {
    put(key, String.valueOf(value));
  }

  public static void remove(String key) {
    ThreadContext.remove(key);
  }

  public static void clearMap() {
    ThreadContext.clearMap();
  }
}
