package com.riiablo.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.riiablo.entity.Entity;

public class Server implements Disposable, Runnable {
  private static final String TAG = "Server";

  private final Json json = new Json();
  private List<Client> clients = new CopyOnWriteArrayList<>();

  ThreadGroup clientThreads;
  ServerSocket server;
  AtomicBoolean kill;
  Thread connectionListener;
  int port;
  String name;
  IntMap<Entity> entities = new IntMap<>(); // TODO: synchronize

  public Server(int port) {
    this(port, "");
  }

  public Server(int port, String name) {
    this.port = port;
    this.name = name;
  }

  @Override
  public void run() {
    clientThreads = new ThreadGroup(name + "-Clients");
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, port, null);
    kill = new AtomicBoolean(false);
    connectionListener = new Thread(new Runnable() {
      @Override
      public void run() {
        Gdx.app.log(name, "listening on " + port);
        while (!kill.get()) {
          try {
            Socket socket = server.accept(null);
            new Client(socket).start();
            Gdx.app.log(name, "connection from " + socket.getRemoteAddress());
          } catch (Throwable t) {
            Gdx.app.log(name, t.getMessage(), t);
          }
        }
      }
    });
    connectionListener.setName("ConnectionListener");
    connectionListener.start();
  }

  @Override
  public void dispose() {
    for (Client client : clients) {
      try {
        client.socket.dispose();
      } catch (Throwable t) {
        Gdx.app.error(name, t.getMessage(), t);
      }
    }

    kill.set(true);
    //try {
    //  connectionListener.join();
    //} catch (InterruptedException ignored) {}
    server.dispose();
  }

  public void update() {
    /*
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    for (ImmutableTriple<Socket, BufferedReader, PrintWriter> client : clients) {
      //System.out.println("client " + (client != null ? client.isConnected() : "false") + " " + client);
      socket = client.left;
      in = client.middle;
      try {
        if (!in.ready()) {
          Gdx.app.log(name, socket.getRemoteAddress() + " disconnected");
          clients.remove(client);
          socket.dispose();
        }
      } catch (IOException e) {
        Gdx.app.error(name, e.getMessage());
      }

      try {
        for (String input; in.ready() && (input = in.readLine()) != null; ) {
          Gdx.app.log(name, socket.getRemoteAddress() + ": " + input);
          for (ImmutableTriple<Socket, BufferedReader, PrintWriter> broadcast : clients) {
            Gdx.app.log(name, "broadcast " + broadcast.left.getRemoteAddress() + ": " + input);
            out = broadcast.right;
            out.println(input);
            if (out.checkError()) {
              Gdx.app.log(name, "error at " + broadcast.left.getRemoteAddress());
            }
          }
        }
      } catch (IOException e) {
        Gdx.app.error(name, e.getMessage(), e);
      }
    }
    */
  }

  private class Client extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    int     id;
    Connect connect;

    public Client(Socket socket) {
      super(clientThreads, "Client-" + String.format("%08X", MathUtils.random(1, Integer.MAX_VALUE - 1)));
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        in = IOUtils.buffer(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        connect = Packets.parse(Connect.class, in.readLine());
        id = connect.id = entities.size + 1;
        entities.put(id, null);

        String connectResponse = Packets.build(new ConnectResponse(id));
        out.println(connectResponse);
        for (Client client : clients) {
          out.println(Packets.build(client.connect));
        }

        String connect = Packets.build(this.connect);
        Gdx.app.log(getName(), connect);
        for (Client client : clients) {
          client.out.println(connect);
          //client.out.println("CONNECT " + socket.getRemoteAddress());
        }

        clients.add(this);

        for (String input; (input = in.readLine()) != null;) {
          //Message message = Packets.parse(Message.class, input);

          String message = "MESSAGE " + socket.getRemoteAddress() + ": " + input;
          Gdx.app.log(getName(), message);

          Packet packet = Packets.parse(input);
          switch (packet.type) {
            case Packets.MESSAGE:
              for (Client client : clients) {
                //client.out.println(message);
                client.out.println(input);
              }
              break;
            case Packets.MOVETO:
              MoveTo moveTo = packet.readValue(MoveTo.class);
              moveTo.id = id;
              input = Packets.build(moveTo);
              for (Client client : clients) {
                if (client == this) continue;
                client.out.println(input);
              }
              break;
          }

        }
      } catch (Throwable t) {
        Gdx.app.log(getName(), "ERROR " + socket.getRemoteAddress() + ": " + t.getMessage());
      } finally {
        entities.remove(id);
        clients.remove(this);
        String message = "DISCONNECT " + socket.getRemoteAddress();
        Gdx.app.log(getName(), message);
        String disconnect = Packets.build(new Disconnect(id, connect.name));
        for (Client client : clients) {
          client.out.println(disconnect);
        }
        //IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        if (socket != null) socket.dispose();
      }
    }

    private void tmp() {
      JsonReader reader = new JsonReader();
    }
  }
}
