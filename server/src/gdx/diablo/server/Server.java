package gdx.diablo.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;

public class Server extends ApplicationAdapter {
  private static final String TAG = "Server";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new Server(), config);
  }

  private Server() {}

  @Override
  public void create() {
    Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));

    try {
      InetAddress address = InetAddress.getLocalHost();
      Gdx.app.log(TAG, "IP Address: " + address.getHostAddress());
      Gdx.app.log(TAG, "Host Name: " + address.getHostName());
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    Gdx.app.log(TAG, "awaiting connection...");

    ServerSocketHints hints = new ServerSocketHints();
    hints.acceptTimeout = 0;

    ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, 6112, hints);

    Socket socket = server.accept(new SocketHints());
    Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());
    socket.dispose();

    Gdx.app.exit();
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "shutting down...");
  }
}
