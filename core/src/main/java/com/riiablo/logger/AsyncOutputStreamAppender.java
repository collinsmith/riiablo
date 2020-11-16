package com.riiablo.logger;

import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AsyncOutputStreamAppender implements Appender, Runnable {
  private final OutputStream out;
  private final Encoder encoder = new RiiabloEncoder();

  private final Thread thread;
  private final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<>(65536);

  public AsyncOutputStreamAppender(OutputStream out) {
    this.out = out;

    thread = new Thread(this);
    thread.setName("AsyncOutputStreamAppender-Worker");
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public Encoder encoder() {
    return encoder;
  }

  @Override
  public void append(LogEvent event) {
    boolean added = queue.offer(event);
    assert added : "event(" + event + ") could not be added to queue!";
  }

  @Override
  public void run() {
    LogEvent event = null;
    for (;;) {
      try {
        event = queue.take();
        encoder.encode(event, out);
      } catch (InterruptedException ignored) {
      } finally {
        if (event != null) {
          event.release();
          event = null;
        }
      }
    }
  }
}
