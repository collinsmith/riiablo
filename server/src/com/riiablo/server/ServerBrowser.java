package com.riiablo.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Json;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerBrowser extends ApplicationAdapter {
  private static final String TAG = "Server";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new ServerBrowser(), config);
  }

  private static final boolean EXT_HOST = true;
  private String host;

  private final Json json = new Json();
  private Map<String, Session> sessions = new ConcurrentHashMap<>();

  ServerSocket server;
  Thread thread;
  AtomicBoolean kill;

  ThreadGroup sessionGroup = new ThreadGroup("Sessions");
  private Map<String, Thread> servers = new ConcurrentHashMap<>();

  ServerBrowser() {}

  private static String getIp() {
    if (!EXT_HOST) {
      try {
        InetAddress address = InetAddress.getLocalHost();
        return address.getHostAddress();
      } catch (UnknownHostException e) {
        Gdx.app.error(TAG, e.getMessage(), e);
        return "hydra";
      }
    }

    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()));
      return in.readLine();
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
      return "hydra";
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public void create() {
    final Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));

    host = getIp();
    try {
      InetAddress address = InetAddress.getLocalHost();
      Gdx.app.log(TAG, "IP Address: " + host);
      Gdx.app.log(TAG, "Host Name: " + address.getHostName());
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    kill = new AtomicBoolean(false);
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, 6112, null);
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!kill.get()) {
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
            } else if (path.equals("/chat")) {
              chat(in, out);
            }
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          } finally {
            //IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            if (socket != null) socket.dispose();
          }
        }
      }
    });
    thread.setName("ServerBrowser");
    thread.start();
  }

  @Override
  public void render() {

  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "shutting down...");
    kill.set(true);
    try {
      thread.join();
    } catch (Throwable ignored) {}
    server.dispose();
  }

  private void getSessions(PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(sessions.values()));
  }

  private void createSession(BufferedReader in, PrintWriter out) {
    String content = ServerUtils.getContent(in);
    Session.Builder builder = json.fromJson(Session.Builder.class, content);

    if (sessions.containsKey(builder.name)) {
      SessionError error = new SessionError(5138, "A game already exists with that name");
      out.print("HTTP/1.1 200\r\n");
      out.print("\r\n");
      out.print(json.toJson(error));
      return;
    } else if (sessions.size() >= 2) {
      SessionError error = new SessionError(5140, "No game server available");
      out.print("HTTP/1.1 200\r\n");
      out.print("\r\n");
      out.print(json.toJson(error));
      return;
    }

    Session session = builder.build();
    session.host = host;
    session.port = 6114 + sessions.size();
    sessions.put(session.getName(), session);

    String id = String.format("%08X", MathUtils.random(1, Integer.MAX_VALUE - 1));
    DedicatedServer server = DedicatedServer.newDedicatedServer(sessionGroup, "Session-" + id, session.port);
    server.start();
    servers.put(session.getName(), server);

    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(session));
  }

  private void findServer(PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
  }

  private void login(BufferedReader in, PrintWriter out) {
    String content = ServerUtils.getContent(in);
    //System.out.println(content);
    Account.Builder builder = json.fromJson(Account.Builder.class, content);

    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
    out.print(json.toJson(builder.build()));
  }

  private void chat(BufferedReader in, PrintWriter out) {
    out.print("HTTP/1.1 200\r\n");
    out.print("\r\n");
  }
}
