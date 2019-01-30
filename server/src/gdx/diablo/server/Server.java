package gdx.diablo.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Json;

import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends Thread {
  private static final String TAG = "Server";

  private final Json json = new Json();

  ServerSocket server;
  AtomicBoolean kill;
  int port;

  Server(ThreadGroup group, String name, int port) {
    super(group, name);
    this.port = port;
    kill = new AtomicBoolean(false);
  }

  @Override
  public void run() {
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, port, null);
    while (!kill.get()) {
      Socket client = null;
      try {
        client = server.accept(null);
        Gdx.app.log(getName(), "connection from " + client.getRemoteAddress());
      } finally {
        if (client != null) client.dispose();
      }
    }
  }
}
