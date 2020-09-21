package com.riiablo.logger.message;

import java.util.Locale;

import com.badlogic.gdx.utils.Pool;

public enum PooledFormattedMessageFactory implements MessageFactory {
  INSTANCE;

  private static final Pool<PooledFormattedMessage> POOL = PooledFormattedMessage.POOL;

  @Override
  public Message newMessage(String message, Object... params) {
    return new FormattedMessage(message, params);
  }

  @Override
  public Message newMessage(String message, Object arg0) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.numArgs = 1;
    msg.throwable = arg0 instanceof Throwable ? (Throwable) arg0 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.numArgs = 2;
    msg.throwable = arg1 instanceof Throwable ? (Throwable) arg1 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.numArgs = 3;
    msg.throwable = arg2 instanceof Throwable ? (Throwable) arg2 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.numArgs = 4;
    msg.throwable = arg3 instanceof Throwable ? (Throwable) arg3 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.numArgs = 5;
    msg.throwable = arg4 instanceof Throwable ? (Throwable) arg4 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.args[5] = arg5;
    msg.numArgs = 6;
    msg.throwable = arg5 instanceof Throwable ? (Throwable) arg5 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.args[5] = arg5;
    msg.args[6] = arg6;
    msg.numArgs = 7;
    msg.throwable = arg6 instanceof Throwable ? (Throwable) arg6 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.args[5] = arg5;
    msg.args[6] = arg6;
    msg.args[7] = arg7;
    msg.numArgs = 8;
    msg.throwable = arg7 instanceof Throwable ? (Throwable) arg7 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.args[5] = arg5;
    msg.args[6] = arg6;
    msg.args[7] = arg7;
    msg.args[8] = arg8;
    msg.numArgs = 9;
    msg.throwable = arg8 instanceof Throwable ? (Throwable) arg8 : null;
    return msg;
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    final PooledFormattedMessage msg = POOL.obtain();
    msg.locale = Locale.getDefault();
    msg.pattern = message;
    msg.args[0] = arg0;
    msg.args[1] = arg1;
    msg.args[2] = arg2;
    msg.args[3] = arg3;
    msg.args[4] = arg4;
    msg.args[5] = arg5;
    msg.args[6] = arg6;
    msg.args[7] = arg7;
    msg.args[8] = arg8;
    msg.args[9] = arg9;
    msg.numArgs = 10;
    msg.throwable = arg9 instanceof Throwable ? (Throwable) arg9 : null;
    return msg;
  }
}
