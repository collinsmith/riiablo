package com.riiablo.server;

import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

public class DedicatedServer extends Thread implements Disposable {
  private static final String TAG = "DedicatedServer";

  Server server;
  AtomicBoolean kill;

  DedicatedServer(ThreadGroup group, String name, Server target) {
    super(group, target, name);
    server = target;
    kill = new AtomicBoolean(false);
  }

  public static DedicatedServer newDedicatedServer(ThreadGroup group, String name, int port) {
    Server server = new Server(port, name);
    return new DedicatedServer(group, name, server);
  }

  @Override
  public void run() {
    super.run();
    while (!kill.get()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {}
      server.update();
    }
  }

  @Override
  public void dispose() {
    kill.set(true);
    //try {
    //  join();
    //} catch (InterruptedException ignored) {}
    server.dispose();
  }
}
