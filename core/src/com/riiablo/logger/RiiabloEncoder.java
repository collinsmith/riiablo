package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private static final int MAX_DEPTH = 256;
  private static final int DEPTH_STEP = 2;
  private final CharSequence spaces = StringUtils.repeat(' ', MAX_DEPTH * DEPTH_STEP);

  private final StringBuilder buffer = new StringBuilder(1024);

  private boolean fullMode;
  private OrderedMap<String, String> mdc;
  private int depth;

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
    encodeFullMDC(mdc, buffer);
  }

  private void encodeFullMDC(OrderedMap<String, String> mdc, StringBuilder buffer) {
    if (mdc.isEmpty()) return;
    buffer.append(' ');
    buffer.append(mdc.toString());
  }

  private void encodeCompactMode(LogEvent event, StringBuilder buffer) {
    final OrderedMap<String, String> mdc = event.mdc();
    final int depth = mdc.size();
    if (depth > 0) {
      if (!mdc.equals(this.mdc) || this.depth != depth) {
        encodeCompactMDC(mdc, buffer, depth);
        this.mdc = mdc;
        this.depth = depth;
      }

      buffer.append(spaces, 0, depth * DEPTH_STEP);
    }

    encodeMessage(event, buffer);
  }

  private void encodeCompactMDC(OrderedMap<String, String> mdc, StringBuilder buffer, int depth) {
    buffer.append(spaces, 0, (depth - 1) * DEPTH_STEP);
    encodeFullMDC(mdc, buffer);
    buffer.append(lineSeparator);
  }
}
