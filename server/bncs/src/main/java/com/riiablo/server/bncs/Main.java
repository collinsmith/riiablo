package com.riiablo.server.bncs;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends ApplicationAdapter {
  private static final String TAG = "D2CS";

  private static final int PORT = 6113;
  private static final int MAX_CLIENTS = 32;

  ServerSocket server;
  ByteBuffer buffer;
  Thread main;
  AtomicBoolean kill;
  ThreadGroup clientThreads;
  final Array<Client> clients = new Array<>(MAX_CLIENTS);
  final Array<Packet> packets = new Array<>(32);
  final Array<Packet> cache = new Array<>(32);

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new Main(), config);
  }

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

    clientThreads = new ThreadGroup("D2CSClients");

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
          synchronized (clients) {
            if (clients.size >= MAX_CLIENTS) {
              try {
                ConnectionDenied(socket, "Server is Full");
              } catch (Throwable ignored) {
              } finally {
                socket.dispose();
              }
            } else {
              try {
                ConnectionAccepted(socket);
                int id = clients.size;
                Client client = new Client(id, socket);
                clients.add(client);
                client.start();
              } catch (Throwable ignored) {
                socket.dispose();
              }
            }
          }
        }

        Gdx.app.log(TAG, "killing child threads...");
        synchronized (clients) {
          for (Client client : clients) {
            if (client != null) {
              client.kill.set(true);
            }
          }

          clients.clear();
        }

        Gdx.app.log(TAG, "killing thread...");
      }
    });
    main.setName("D2CS");
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
    synchronized (packets) {
      cache.clear();
      cache.addAll(packets);
      packets.clear();
    }

    for (Packet packet : cache) {
      process(packet);
    }
  }

  private void process(Packet packet) {
    synchronized (clients) {
      for (Client client : clients) {
        try {
          client.socket.getOutputStream().write(packet.data);
        } catch (Throwable t) {
          Gdx.app.error(TAG, t.getMessage(), t);
          client.kill.set(true);
        }
      }
    }
//    BNCS data = packet.data;
//    switch (data.dataType()) {
//      case BNCSData.ChatEvent:
//        synchronized (clients) {
//          for (Client client : clients) {
//          }
//        }
//        break;
//      default:
//        Gdx.app.error(TAG, "Unknown packet type: " + data.dataType());
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

  private void process(int id, byte[] packet) {
    synchronized (packets) {
      packets.add(Packet.of(id, packet));
    }
  }

  static String generateClientName() {
    return String.format("Client-%08X", MathUtils.random(1, Integer.MAX_VALUE - 1));
  }

  private class Client extends Thread {
    int id;
    Socket socket;
    AtomicBoolean kill = new AtomicBoolean(false);

    Client(int id, Socket socket) {
      super(clientThreads, generateClientName());
      this.id = id;
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

          byte[] data = com.riiablo.util.BufferUtils.readRemaining(buffer);

          /*
          BNCS packet = BNCS.getRootAsBNCS(buffer);
          Gdx.app.log(TAG, "packet type " + D2GSData.name(packet.dataType()));
          */
          process(id, data);
        } catch (Throwable t) {
          Gdx.app.log(TAG, t.getMessage(), t);
          kill.set(true);
        }
      }

      Gdx.app.log(TAG, "closing socket...");
      if (socket != null) socket.dispose();
      synchronized (clients) {
        clients.removeValue(this, true);
      }
    }
  }

  private static class Packet {
    int id;
    byte[] data;

    static Packet of(int id, byte[] data) {
      Packet packet = new Packet();
      packet.id = id;
      packet.data = data;
      return packet;
    }
  }
}
