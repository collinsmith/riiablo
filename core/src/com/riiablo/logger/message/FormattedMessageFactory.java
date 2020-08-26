package com.riiablo.logger.message;

public enum FormattedMessageFactory implements MessageFactory {
  INSTANCE;

  @Override
  public Message newMessage(final String message, final Object... params) {
    return new FormattedMessage(message, params);
  }
}
