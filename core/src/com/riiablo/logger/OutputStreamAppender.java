package com.riiablo.logger;

import java.io.OutputStream;

public class OutputStreamAppender implements Appender {
  private final OutputStream out;
  private final Layouter layouter = new RiiabloLayouter();

  public OutputStreamAppender(OutputStream out) {
    this.out = out;
  }

  @Override
  public Layouter layout() {
    return layouter;
  }

  @Override
  public void append(LogEvent event) {
    layouter.encode(event, out);
  }
}
