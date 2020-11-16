package com.riiablo.logger;

import java.io.OutputStream;

public interface Encoder {
  void encode(LogEvent event, OutputStream out);
}
