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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends ApplicationAdapter {
  private static final String TAG = "Server";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new Server(), config);
  }

  private final Json json = new Json();
  private Array<Session> sessions = new Array<>(new Session[] {
    new Session("Kmbaal-33"),
    new Session("Cbaalz73"),
    new Session("Killin Foos"),
    new Session("Skulders 4 Scri"),
  });

  ServerSocket server;
  Thread serverThread;
  AtomicBoolean killServer;

  Server() {}

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

    server = Gdx.net.newServerSocket(Net.Protocol.TCP, 6112, null);
    killServer = new AtomicBoolean(false);
    serverThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!killServer.get()) {
          Socket socket = null;
          BufferedReader in = null;
          PrintWriter out = null;
          try {
            socket = server.accept(null);

            Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());

            in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), false);

            String statusLine = in.readLine();
            Gdx.app.log(TAG, statusLine);
            String[] parts = statusLine.split("\\s+", 3);

            String path = parts[1];
            if (path.equals("/get-sessions")) {
              getSessions(out);
            } else if (path.equals("/create-session")) {
              createSession(in, out);
            } else if (path.equals("/find-server")) {
              findServer(out);
            } else if (path.equals("/login")) {
              login(in, out);
            }
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          } finally {
            IOUtils.closeQuietly(out);
            //IOUtils.closeQuietly(in);
            if (socket != null) socket.dispose();
          }
        }
      }
    });
    serverThread.start();
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "shutting down...");
    killServer.set(true);
    try {
      serverThread.join();
    } catch (Throwable ignored) {}
    server.dispose();
  }

  private void getSessions(PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(sessions));
  }

  private void createSession(BufferedReader in, PrintWriter out) {
    String content = getContent(in);
    //System.out.println(content);
    Session.Builder builder = json.fromJson(Session.Builder.class, content);

    sessions.add(builder.build());

    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(builder.build()));
  }

  private void findServer(PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
  }

  private void login(BufferedReader in, PrintWriter out) {
    String content = getContent(in);
    //System.out.println(content);
    Account.Builder builder = json.fromJson(Account.Builder.class, content);

    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(builder.build()));
  }

  /**
   * TODO: parse packet content-length and read that many chars into a string
   */
  public String getContent(BufferedReader reader) {
    try {
      int length = -1;
      for (String str; (str = reader.readLine()) != null && !str.isEmpty();) {
        if (StringUtils.startsWithIgnoreCase(str, "Content-Length:")) {
          str = StringUtils.replaceIgnoreCase(str, "Content-Length:", "").trim();
          length = NumberUtils.toInt(str, length);
        }
      }
      //return reader.readLine();
      char[] chars = new char[length];
      reader.read(chars);
      return new String(chars);
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
      return null;
    }
  }
}
