package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private final StringBuilder buffer = new StringBuilder(1024);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      encodeMessage(event, buffer);
      out.write(buffer.toString().getBytes(US_ASCII));
      newLine(out);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }
}
