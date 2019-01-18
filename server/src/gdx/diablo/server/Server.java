package gdx.diablo.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

  private Array<Session> sessions = new Array<>(new Session[] {
    new Session("Kmbaal-33"),
    new Session("Cbaalz73"),
    new Session("Killin Foos"),
    new Session("Skulders 4 Scri"),
  });

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

    ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, 6112, null);
    while (true) {
      Socket socket = null;
      BufferedReader in = null;
      PrintWriter out = null;
      try {
        socket = server.accept(null);

        Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());

        in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        String statusLine = in.readLine();
        Gdx.app.log(TAG, statusLine);
        String[] parts = statusLine.split("\\s+", 3);

        String path = parts[1];
        if (path.startsWith("/get-sessions")) {
          getSessions(out);
        } else if (path.startsWith("/create-session")) {
          createSession(in, out);
        }
      } catch (Throwable t) {
        Gdx.app.error(TAG, t.getMessage(), t);
      } finally {
        IOUtils.closeQuietly(out);
        IOUtils.closeQuietly(in);
        socket.dispose();
      }

      try {
      } finally {
      }

      socket.dispose();
    }
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "shutting down...");
  }

  private void getSessions(PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(new Json().toJson(sessions));
  }

  private void createSession(BufferedReader in, PrintWriter out) {
    try {
      for (String str; (str = in.readLine()) != null && !str.isEmpty(););
      Session.Builder builder = new Json().fromJson(Session.Builder.class, in);
      sessions.add(builder.build());
      System.out.println(builder);
    } catch (IOException e) {}
  }
}
