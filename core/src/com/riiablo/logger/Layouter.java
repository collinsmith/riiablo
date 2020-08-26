package com.riiablo.logger;

import java.io.OutputStream;

public interface Layouter {
  void encode(LogEvent event, OutputStream out);
}
