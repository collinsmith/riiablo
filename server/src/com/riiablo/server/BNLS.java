package com.riiablo.server;

import com.google.flatbuffers.FlatBufferBuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.BufferUtils;
import com.riiablo.net.packet.bnls.BNLSData;
import com.riiablo.net.packet.bnls.ConnectionAccepted;
import com.riiablo.net.packet.bnls.ConnectionClosed;
import com.riiablo.net.packet.bnls.LoginResponse;
import com.riiablo.net.packet.bnls.QueryRealms;
import com.riiablo.net.packet.bnls.Realm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BNLS extends ApplicationAdapter {
  private static final String TAG = "BNLS";

  private static final int PORT = 6110;
  private static final int MAX_CLIENTS = 32;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new BNLS(), config);
  }

  ServerSocket server;
  ByteBuffer buffer;
  Thread main;
  AtomicBoolean kill;
  Thread cli;
  ThreadGroup clientThreads;
  CopyOnWriteArrayList<Client> CLIENTS = new CopyOnWriteArrayList<>();

  private static final String[][] REALMS = new String[][] {
      {"localhost", "U.S. West"},
      {"localhost", "PTR"},
  };

  BNLS() {}

  @Override
  public void create() {
    final Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));

    try {
      InetAddress address = InetAddress.getLocalHost();
      Gdx.app.log(TAG, "IP Address: " + address.getHostAddress() + ":" + PORT);
      Gdx.app.log(TAG, "Host Name: " + address.getHostName());
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    clientThreads = new ThreadGroup("BNLSClients");

    Gdx.app.log(TAG, "Starting server...");
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, null);
    buffer = BufferUtils.newByteBuffer(4096);
    kill = new AtomicBoolean(false);
    main = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!kill.get()) {
          Gdx.app.log(TAG, "waiting...");
          Socket socket = server.accept(null);
          Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());
          if (CLIENTS.size() >= MAX_CLIENTS) {
            try {
              ConnectionDenied(socket, "Server is Full");
            } catch (Throwable ignored) {
            } finally {
              socket.dispose();
            }
          } else {
            try {
              ConnectionAccepted(socket);
              new Client(socket).start();
            } catch (Throwable ignored) {
              socket.dispose();
            }
          }
        }

        Gdx.app.log(TAG, "killing child threads...");
        for (Client client : CLIENTS) {
          if (client != null) {
            client.kill.set(true);
          }
        }

        Gdx.app.log(TAG, "killing thread...");
      }
    });
    main.setName("BNLS");
    main.start();

    cli = new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!kill.get()) {
          try {
            if (!reader.ready()) continue;
            String in = reader.readLine();
            if (in.equalsIgnoreCase("exit")) {
              Gdx.app.exit();
            } else if (in.equalsIgnoreCase("realms")) {
              Gdx.app.log(TAG, "realms:");
              for (String[] realms : REALMS) {
                Gdx.app.log(TAG, "  " + realms[0] + " " + realms[1]);
              }
            }
          } catch (Throwable t) {
            Gdx.app.log(TAG, t.getMessage());
          }
        }
      }
    });
    cli.setName("CLI");
    cli.start();
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "Shutting down...");
    kill.set(true);
    server.dispose();
    try {
      main.join();
    } catch (Throwable ignored) {}
  }

  private void process(Socket socket, com.riiablo.net.packet.bnls.BNLS packet) throws IOException {
    switch (packet.dataType()) {
      case BNLSData.QueryRealms:
        QueryRealms(socket);
        break;
      case BNLSData.LoginResponse:
        LoginResponse(socket, packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private boolean ConnectionDenied(Socket socket, String reason) throws IOException {
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int reasonOffset = builder.createString(reason);
    int connectionDeniedId = ConnectionClosed.createConnectionClosed(builder, reasonOffset);
    int id = com.riiablo.net.packet.bnls.BNLS.createBNLS(builder, BNLSData.ConnectionClosed, connectionDeniedId);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    return true;
  }

  private boolean ConnectionAccepted(Socket socket) throws IOException {
    Gdx.app.debug(TAG, "Connection accepted!");
    FlatBufferBuilder builder = new FlatBufferBuilder();
    ConnectionAccepted.startConnectionAccepted(builder);
    int connectionAcceptedId = ConnectionAccepted.endConnectionAccepted(builder);
    int id = com.riiablo.net.packet.bnls.BNLS.createBNLS(builder, BNLSData.ConnectionAccepted, connectionAcceptedId);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    return false;
  }

  private boolean QueryRealms(Socket socket) throws IOException {
    FlatBufferBuilder builder = new FlatBufferBuilder();

    int[] realms = new int[REALMS.length];
    for (int i = 0; i < REALMS.length; i++) {
      realms[i] = Realm.createRealm(builder, builder.createString(REALMS[i][0]), builder.createString(REALMS[i][1]));
    }
    int realmsVec = QueryRealms.createRealmsVector(builder, realms);

    QueryRealms.startQueryRealms(builder);
    QueryRealms.addRealms(builder, realmsVec);
    int realmId = QueryRealms.endQueryRealms(builder);

    int id = com.riiablo.net.packet.bnls.BNLS.createBNLS(builder, BNLSData.QueryRealms, realmId);

    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();

    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.log(TAG, "returning realms list...");
    return false;
  }

  private boolean LoginResponse(Socket socket, com.riiablo.net.packet.bnls.BNLS packet) throws IOException {
    LoginResponse request = (LoginResponse) packet.data(new LoginResponse());
    String username = request.username();
    Gdx.app.log(TAG, "Login request from username " + username);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    int usernameOffset = builder.createString(username);
    int offset = LoginResponse.createLoginResponse(builder, usernameOffset);
    int id = com.riiablo.net.packet.bnls.BNLS.createBNLS(builder, BNLSData.LoginResponse, offset);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();

    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.log(TAG, "returning login response...");
    return true;
  }

  static String generateClientName() {
    return String.format("Client-%08X", MathUtils.random(1, Integer.MAX_VALUE - 1));
  }

  private class Client extends Thread {
    Socket socket;
    AtomicBoolean kill = new AtomicBoolean(false);

    Client(Socket socket) {
      super(clientThreads, generateClientName());
      this.socket = socket;
    }

    @Override
    public void run() {
      while (!kill.get()) {
        try {
          buffer.clear();
          buffer.mark();
          ReadableByteChannel in = Channels.newChannel(socket.getInputStream());
          if (in.read(buffer) == -1) {
            kill.set(true);
            break;
          }
          buffer.limit(buffer.position());
          buffer.reset();

          com.riiablo.net.packet.bnls.BNLS packet = com.riiablo.net.packet.bnls.BNLS.getRootAsBNLS(buffer);
          Gdx.app.log(TAG, "packet type " + BNLSData.name(packet.dataType()));
          process(socket, packet);
        } catch (Throwable t) {
          Gdx.app.log(TAG, t.getMessage(), t);
          kill.set(true);
        }
      }

      Gdx.app.log(TAG, "closing socket...");
      if (socket != null) socket.dispose();
    }
  }
}
