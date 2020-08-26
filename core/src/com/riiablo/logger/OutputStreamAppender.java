package com.riiablo.logger;

import java.io.OutputStream;

public class OutputStreamAppender implements Appender {
  private final OutputStream out;
  private final Encoder encoder = new RiiabloEncoder();

  public OutputStreamAppender(OutputStream out) {
    this.out = out;
  }

  @Override
  public Encoder encoder() {
    return encoder;
  }

  @Override
  public void append(LogEvent event) {
    encoder.encode(event, out);
  }
}
