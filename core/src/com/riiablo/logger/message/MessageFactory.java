package com.riiablo.logger.message;

public interface MessageFactory {
  Message newMessage(final String message, final Object... params);
}
