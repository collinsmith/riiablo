package com.riiablo.logger.message;

public interface MessageFactory {
  Message newMessage(final String message, final Object... params);
  Message newMessage(final String message, final Object arg0);
  Message newMessage(final String message, final Object arg0, final Object arg1);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5, final Object arg6);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8);
  Message newMessage(final String message, final Object arg0, final Object arg1, final Object arg2, final Object arg3, final Object arg4, final Object arg5, final Object arg6, final Object arg7, final Object arg8, final Object arg9);
}
