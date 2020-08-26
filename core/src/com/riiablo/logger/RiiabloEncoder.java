package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private final StringBuilder buffer = new StringBuilder(1024);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      final OrderedMap<String, String> mdc = event.mdc();
      encodeMessage(event, buffer);
      encodeMDC(mdc, buffer);
      out.write(buffer.toString().getBytes(US_ASCII));
      newLine(out);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }

  private void encodeMDC(OrderedMap<String, String> mdc, StringBuilder buffer) {
    if (mdc.isEmpty()) return;
    buffer.append(' ');
    buffer.append(mdc.toString());
  }
}
