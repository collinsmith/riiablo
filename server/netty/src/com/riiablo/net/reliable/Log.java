package com.riiablo.net.reliable;

import com.badlogic.gdx.Gdx;

public class Log {
  private Log() {}

  public static void error(String tag, String message) {
    Gdx.app.error(tag, message);
  }

  public static void error(String tag, String format, Object... args) {
    error(tag, String.format(format, args));
  }

  public static void log(String tag, String message) {
    Gdx.app.log(tag, message);
  }

  public static void log(String tag, String format, Object... args) {
    log(tag, String.format(format, args));
  }

  public static void debug(String tag, String message) {
    Gdx.app.debug(tag, message);
  }

  public static void debug(String tag, String format, Object... args) {
    debug(tag, String.format(format, args));
  }
}
