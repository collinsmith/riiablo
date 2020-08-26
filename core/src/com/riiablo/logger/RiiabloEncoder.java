package com.riiablo.logger;

import java.io.OutputStream;

public class RiiabloEncoder extends SimpleEncoder {
  private final StringBuilder buffer = new StringBuilder(1024);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    encodeMessage(event, out);
  }

  private void encodeMessage(LogEvent event, OutputStream out) {
    super.encode(event, out);
  }
}
