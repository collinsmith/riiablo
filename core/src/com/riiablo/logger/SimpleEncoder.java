package com.riiablo.logger;

import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SimpleEncoder implements Encoder {
  static final Charset US_ASCII = Charset.forName("US-ASCII");
  final byte[] newLine = System.getProperty("line.separator").getBytes(US_ASCII);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      out.write(event.message().format().getBytes(US_ASCII));
      out.write(newLine);
      out.flush();
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    }
  }
}
