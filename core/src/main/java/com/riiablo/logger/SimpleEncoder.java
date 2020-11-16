package com.riiablo.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SimpleEncoder implements Encoder {
  static final Charset US_ASCII = Charset.forName("US-ASCII");
  final CharSequence lineSeparator = System.getProperty("line.separator");
  final byte[] newLine = lineSeparator.toString().getBytes(US_ASCII);

  private final StringBuilder buffer = new StringBuilder(1024);

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      encodeMessage(event, buffer);
      out.write(buffer.toString().getBytes(US_ASCII));
      newLine(out);
      encodeStackTrace(event, out);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }

  protected void newLine(OutputStream out) throws IOException {
    out.write(newLine);
    out.flush();
  }

  protected void encodeMessage(LogEvent event, StringBuilder buffer) {
    buffer.append(StringUtils.rightPad(event.level().name(), 5));
    buffer.append(' ');
    buffer.append('[');
    buffer.append(ClassUtils.getShortClassName(event.source().getClassName()));
    buffer.append(']');
    buffer.append(' ');
    buffer.append(event.message().format());
  }

  protected void encodeStackTrace(LogEvent event, OutputStream out) {
    final Throwable throwable = event.message().throwable();
    if (throwable == null) return;
    throwable.printStackTrace(new PrintStream(out));
  }
}
