package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private final StringBuilder buffer = new StringBuilder(1024);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      buffer.append(StringUtils.rightPad(event.level().name(), 5));
      buffer.append(' ');
      buffer.append('[');
      buffer.append(ClassUtils.getShortClassName(event.source().getClassName()));
      buffer.append(']');
      buffer.append(' ');
      buffer.append(event.message().format());
      out.write(buffer.toString().getBytes(US_ASCII));
      out.write(newLine);
      out.flush();
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }
}
