package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private final StringBuilder buffer = new StringBuilder(1024);

  private boolean fullMode;

  public boolean isFullMode() {
    return fullMode;
  }

  public void setFullMode(boolean b) {
    this.fullMode = b;
  }

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      if (fullMode) {
        encodeFullMode(event, buffer);
      } else {
        encodeCompactMode(event, buffer);
      }
      out.write(buffer.toString().getBytes(US_ASCII));
      newLine(out);
      encodeStackTrace(event, out);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }

  private void encodeFullMode(LogEvent event, StringBuilder buffer) {
    final OrderedMap<String, String> mdc = event.mdc();
    encodeMessage(event, buffer);
    encodeMDC(mdc, buffer);
  }

  private void encodeCompactMode(LogEvent event, StringBuilder buffer) {
    encodeFullMode(event, buffer);
  }

  private void encodeMDC(OrderedMap<String, String> mdc, StringBuilder buffer) {
    if (mdc.isEmpty()) return;
    buffer.append(' ');
    buffer.append(mdc.toString());
  }
}
