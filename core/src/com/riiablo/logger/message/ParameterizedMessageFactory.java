package com.riiablo.logger.message;

public enum ParameterizedMessageFactory implements MessageFactory {
  INSTANCE;

  @Override
  public Message newMessage(final String message, final Object... params) {
    return new ParameterizedMessage(message, params);
  }
}
