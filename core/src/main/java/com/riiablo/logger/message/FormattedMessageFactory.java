package com.riiablo.logger.message;

public enum FormattedMessageFactory implements MessageFactory {
  INSTANCE;

  @Override
  public Message newMessage(final String message, final Object... params) {
    return new FormattedMessage(message, params);
  }

  @Override
  public Message newMessage(String message, Object arg0) {
    return new FormattedMessage(message, arg0);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1) {
    return new FormattedMessage(message, arg0, arg1);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2) {
    return new FormattedMessage(message, arg0, arg1, arg2);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4, arg5);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
  }

  @Override
  public Message newMessage(String message, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
    return new FormattedMessage(message, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
  }
}
