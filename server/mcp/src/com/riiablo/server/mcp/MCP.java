package com.riiablo.server.mcp;

import com.google.flatbuffers.FlatBufferBuilder;

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
import com.riiablo.net.GameSession;
import com.riiablo.net.packet.bnls.ConnectionAccepted;
import com.riiablo.net.packet.bnls.ConnectionClosed;
import com.riiablo.net.packet.mcp.CreateGame;
import com.riiablo.net.packet.mcp.JoinGame;
import com.riiablo.net.packet.mcp.ListGames;
import com.riiablo.net.packet.mcp.MCPData;
import com.riiablo.net.packet.mcp.Result;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MCP extends ApplicationAdapter {
  private static final String TAG = "MCP";

  private static final int PORT = 6111;
  private static final int MAX_CLIENTS = 32;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new MCP(), config);
  }

  ServerSocket server;
  ByteBuffer buffer;
  Thread main;
  AtomicBoolean kill;
  Thread cli;
  ThreadGroup clientThreads;
  CopyOnWriteArrayList<Client> CLIENTS = new CopyOnWriteArrayList<>();

  Map<String, GameSession> sessions = new ConcurrentHashMap<>();
  {
    sessions.put("test1", new GameSession() {{
      this.name = "test1";
      this.desc = "desc1";
    }});
    sessions.put("test2", new GameSession() {{
      this.name = "test2";
      this.desc = "desc2";
    }});
    sessions.put("test3", new GameSession() {{
      this.name = "test3";
      this.desc = "desc3";
    }});
  }

  MCP() {}

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

    clientThreads = new ThreadGroup("MCPClients");

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
    main.setName("MCP");
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
            } else if (in.equalsIgnoreCase("games")) {
              Gdx.app.log(TAG, "games:");
              for (GameSession session : sessions.values()) {
                Gdx.app.log(TAG, "  " + session);
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

  private void process(Socket socket, com.riiablo.net.packet.mcp.MCP packet) throws IOException {
    switch (packet.dataType()) {
      case MCPData.CreateGame:
        CreateGame(socket, packet);
        break;
      case MCPData.JoinGame:
        JoinGame(socket, packet);
        break;
      case MCPData.ListGames:
        ListGames(socket, packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.dataType());
    }
  }

  private boolean ConnectionDenied(Socket socket, String reason) throws IOException {
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int reasonOffset = builder.createString(reason);
    int connectionDeniedId = ConnectionClosed.createConnectionClosed(builder, reasonOffset);
    int id = com.riiablo.net.packet.mcp.MCP.createMCP(builder, MCPData.ConnectionClosed, connectionDeniedId);
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
    int id = com.riiablo.net.packet.mcp.MCP.createMCP(builder, MCPData.ConnectionAccepted, connectionAcceptedId);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    return false;
  }

  private boolean ListGames(Socket socket, com.riiablo.net.packet.mcp.MCP packet) throws IOException {
    ListGames listGames = (ListGames) packet.data(new ListGames());
    Gdx.app.debug(TAG, "Games list requested by " + socket.getRemoteAddress());

    FlatBufferBuilder builder = new FlatBufferBuilder();

    int i = 0;
    int[] sessions = new int[this.sessions.size()];
    for (GameSession session : this.sessions.values()) {
      sessions[i++] = com.riiablo.net.packet.mcp.GameSession.createGameSession(builder, i, 0, builder.createString(session.name), builder.createString(session.desc), 0);
    }

    int sessionsVec = ListGames.createGamesVector(builder, sessions);

    ListGames.startListGames(builder);
    ListGames.addGames(builder, sessionsVec);
    int listGamesOffset = ListGames.endListGames(builder);
    int id = com.riiablo.net.packet.mcp.MCP.createMCP(builder, MCPData.ListGames, listGamesOffset);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.log(TAG, "returning games list...");
    return false;
  }

  private boolean CreateGame(Socket socket, com.riiablo.net.packet.mcp.MCP packet) throws IOException {
    CreateGame createGame = (CreateGame) packet.data(new CreateGame());
    String gameName = createGame.gameName();
    Gdx.app.debug(TAG, "Attempting to create " + gameName + " for " + socket.getRemoteAddress());

    FlatBufferBuilder builder = new FlatBufferBuilder();
    CreateGame.startCreateGame(builder);
    if (sessions.containsKey(gameName)) {
      CreateGame.addResult(builder, Result.ALREADY_EXISTS);
    } else if (sessions.size() >= 4) {
      CreateGame.addResult(builder, Result.SERVER_DOWN);
    } else {
      CreateGame.addResult(builder, Result.SUCCESS);
      sessions.put(gameName, new GameSession(createGame));
      Gdx.app.debug(TAG, "Created session " + gameName);
    }

    int createGameOffset = CreateGame.endCreateGame(builder);
    int id = com.riiablo.net.packet.mcp.MCP.createMCP(builder, MCPData.CreateGame, createGameOffset);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.log(TAG, "returning game creation response...");
    return false;
  }

  private boolean JoinGame(Socket socket, com.riiablo.net.packet.mcp.MCP packet) throws IOException {
    JoinGame joinGame = (JoinGame) packet.data(new JoinGame());
    String gameName = joinGame.gameName();
    Gdx.app.debug(TAG, "Attempting to join " + gameName + " for " + socket.getRemoteAddress());
    GameSession session = sessions.get(gameName);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    JoinGame.startJoinGame(builder);
    if (session == null) {
      JoinGame.addResult(builder, Result.GAME_DOES_NOT_EXIST);
    } else if (session.numPlayers >= Engine.MAX_PLAYERS) {
      JoinGame.addResult(builder, Result.GAME_IS_FULL);
    } else {
      JoinGame.addResult(builder, Result.SUCCESS);
      JoinGame.addIp(builder, session.ip);
      JoinGame.addPort(builder, session.port);
      Gdx.app.debug(TAG, "Sending session info for " + gameName);
    }

    int joinGameOffset = JoinGame.endJoinGame(builder);
    int id = com.riiablo.net.packet.mcp.MCP.createMCP(builder, MCPData.JoinGame, joinGameOffset);
    builder.finish(id);

    ByteBuffer data = builder.dataBuffer();
    OutputStream out = socket.getOutputStream();
    WritableByteChannel channel = Channels.newChannel(out);
    channel.write(data);
    Gdx.app.log(TAG, "returning game join response...");
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

          com.riiablo.net.packet.mcp.MCP packet = com.riiablo.net.packet.mcp.MCP.getRootAsMCP(buffer);
          Gdx.app.log(TAG, "packet type " + MCPData.name(packet.dataType()));
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
