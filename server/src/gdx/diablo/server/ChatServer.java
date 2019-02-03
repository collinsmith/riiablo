package gdx.diablo.server;

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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer extends ApplicationAdapter {
  private static final String TAG = "ChatServer";

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new ChatServer(), config);
  }

  private final Json json = new Json();
  private Set<PrintWriter> clients = new CopyOnWriteArraySet<>();

  ThreadGroup clientThreads;
  ServerSocket server;
  Thread thread;
  AtomicBoolean kill;

  ChatServer() {}

  @Override
  public void create() {
    final Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));

    try {
      InetAddress address = InetAddress.getLocalHost();
      Gdx.app.log(TAG, "IP Address: " + address.getHostAddress());
      Gdx.app.log(TAG, "Host Name: " + address.getHostName());
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    clientThreads = new ThreadGroup("Clients");

    kill = new AtomicBoolean(false);
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, 6113, null);
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!kill.get()) {
          Socket socket = server.accept(null);
          Gdx.app.log(TAG, "CONNECT " + socket.getRemoteAddress());
          new Client(socket).start();
        }
      }
    });
    thread.setName("ChatServer");
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

  private class Client extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    public Client(Socket socket) {
      super(clientThreads, "Client-" + String.format("%08X", MathUtils.random(1, Integer.MAX_VALUE - 1)));
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        final Calendar calendar = Calendar.getInstance();
        DateFormat format = DateFormat.getDateTimeInstance();
        out.println("BNET " + format.format(calendar.getTime()));
        for (PrintWriter client : clients) {
          client.println("CONNECT " + socket.getRemoteAddress());
        }

        clients.add(out);

        for (String input; (input = in.readLine()) != null; ) {
          String message = "MESSAGE " + socket.getRemoteAddress() + ": " + input;
          Gdx.app.log(TAG, message);
          for (PrintWriter client : clients) {
            client.println(message);
          }
        }

      } catch (Throwable t) {
        Gdx.app.log(TAG, "ERROR " + socket.getRemoteAddress() + ": " + t.getMessage());
      } finally {
        String message = "DISCONNECT " + socket.getRemoteAddress();
        Gdx.app.log(TAG, message);
        for (PrintWriter client : clients) {
          client.println(message);
        }
        //IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        if (out != null) clients.remove(out);
        if (socket != null) socket.dispose();
      }
    }
  }
}
