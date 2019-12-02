package com.riiablo.server.d2gs;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.BufferUtils;
import com.riiablo.engine.Engine;
import com.riiablo.net.packet.d2gs.D2GSData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class D2GS extends ApplicationAdapter {
  private static final String TAG = "D2GS";

  private static final int PORT = 6114;
  private static final int MAX_CLIENTS = Engine.MAX_PLAYERS;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new D2GS(), config);
  }

  ServerSocket server;
  ByteBuffer buffer;
  Thread main;
  AtomicBoolean kill;
  ThreadGroup clientThreads;
  CopyOnWriteArrayList<Client> CLIENTS = new CopyOnWriteArrayList<>();

  final BlockingQueue<com.riiablo.net.packet.d2gs.D2GS> packets = new ArrayBlockingQueue<>(32);
  final Collection<com.riiablo.net.packet.d2gs.D2GS> cache = new ArrayList<>();

  D2GS() {}

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

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

    clientThreads = new ThreadGroup("D2GSClients");

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
    main.setName("D2GS");
    main.start();
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

  @Override
  public void render() {
    cache.clear();
    packets.drainTo(cache);
    for (com.riiablo.net.packet.d2gs.D2GS packet : cache) {
      Gdx.app.log(TAG, "processing packet from " + packet);
      process(null, packet);
    }
  }

  private void process(Socket socket, com.riiablo.net.packet.d2gs.D2GS packet) {
//    switch (packet.dataType()) {
//      case BNLSData.QueryRealms:
//        QueryRealms(socket);
//        break;
//      case BNLSData.LoginResponse:
//        LoginResponse(socket, packet);
//        break;
//      default:
//        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
//    }
  }

  private boolean ConnectionDenied(Socket socket, String reason) throws IOException {
//    FlatBufferBuilder builder = new FlatBufferBuilder();
//    int reasonOffset = builder.createString(reason);
//    int connectionDeniedId = ConnectionClosed.createConnectionClosed(builder, reasonOffset);
//    int id = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.ConnectionClosed, connectionDeniedId);
//    builder.finish(id);
//
//    ByteBuffer data = builder.dataBuffer();
//    OutputStream out = socket.getOutputStream();
//    WritableByteChannel channel = Channels.newChannel(out);
//    channel.write(data);
    return true;
  }

  private boolean ConnectionAccepted(Socket socket) throws IOException {
//    Gdx.app.debug(TAG, "Connection accepted!");
//    FlatBufferBuilder builder = new FlatBufferBuilder();
//    ConnectionAccepted.startConnectionAccepted(builder);
//    int connectionAcceptedId = ConnectionAccepted.endConnectionAccepted(builder);
//    int id = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.ConnectionAccepted, connectionAcceptedId);
//    builder.finish(id);
//
//    ByteBuffer data = builder.dataBuffer();
//    OutputStream out = socket.getOutputStream();
//    WritableByteChannel channel = Channels.newChannel(out);
//    channel.write(data);
    return false;
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

          com.riiablo.net.packet.d2gs.D2GS packet = com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(buffer);
          Gdx.app.log(TAG, "packet type " + D2GSData.name(packet.dataType()));
          boolean success = packets.offer(packet);
          if (!success) {
            Gdx.app.log(TAG, "queue full -- kicking client");
            kill.set(true);
          }
        } catch (Throwable t) {
          Gdx.app.log(TAG, t.getMessage(), t);
          kill.set(true);
        }
      }

      Gdx.app.log(TAG, "closing socket...");
      if (socket != null) socket.dispose();
      CLIENTS.remove(this);
    }
  }
}
