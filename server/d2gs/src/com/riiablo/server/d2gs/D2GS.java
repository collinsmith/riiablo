package com.riiablo.server.d2gs;

import com.google.flatbuffers.FlatBufferBuilder;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.riiablo.COFs;
import com.riiablo.CharData;
import com.riiablo.CharacterClass;
import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.D2;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.AnimDataResolver;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.ObjectInitializer;
import com.riiablo.engine.server.ServerEntityFactory;
import com.riiablo.engine.server.ServerNetworkIdManager;
import com.riiablo.map.Map;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.util.DebugUtils;

import net.mostlyoriginal.api.event.common.EventSystem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    Options options = new Options()
        .addOption("home", true, "directory containing D2 MPQ files")
        .addOption("seed", true, "seed used to generate map")
        .addOption("diff", true, "difficulty (0-2)");

    CommandLine cmd = null;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (Throwable t) {
      System.err.println(t.getMessage());
      System.out.println("Failed to start server instance!");
      return;
    }

    FileHandle home = null;
    if (cmd != null && cmd.hasOption("home")) {
      home = new FileHandle(cmd.getOptionValue("home"));
      if (!home.child("d2data.mpq").exists()) {
        throw new GdxRuntimeException("home does not refer to a valid D2 installation");
      }
    } else {
      home = new FileHandle(System.getProperty("user.home")).child("diablo");
      System.out.println("Home not specified, using " + home);
      if (!home.exists() || !home.child("d2data.mpq").exists()) {
        throw new GdxRuntimeException("home does not refer to a valid D2 installation");
      }
    }

    int seed = 0;
    if (cmd.hasOption("seed")) {
      String seedArg = cmd.getOptionValue("seed");
      try {
        seed = Integer.parseInt(seedArg);
      } catch (Throwable t) {
        System.err.println("Invalid seed provided: " + seedArg);
      }
    }

    int diff = 0;
    if (cmd.hasOption("diff")) {
      String diffArg = cmd.getOptionValue("diff");
      try {
        diff = Integer.parseInt(diffArg);
      } catch (Throwable t) {
        System.err.println("Invalid diff provided: " + diffArg);
      }
    }

    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new D2GS(home, seed, diff), config);
  }

  ServerSocket server;
  Thread connectionListener;
  AtomicBoolean kill;
  ThreadGroup clientThreads;
  CopyOnWriteArrayList<Client> CLIENTS = new CopyOnWriteArrayList<>();

  final BlockingQueue<Packet> packets = new ArrayBlockingQueue<>(32);
  final Collection<Packet> cache = new ArrayList<>();
  final BlockingQueue<Packet> outPackets = new ArrayBlockingQueue<>(32);
  final IntIntMap player = new IntIntMap();

  FileHandle home;
  int seed;
  int diff;

  World world;
  Map map;

  D2GS(FileHandle home, int seed, int diff) {
    this.home = home;
    this.seed = seed;
    this.diff = diff;
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

    Riiablo.home = home = Gdx.files.absolute(home.path());
    if (!home.exists() || !home.child("d2data.mpq").exists()) {
      throw new GdxRuntimeException("home does not refer to a valid D2 installation. Copy MPQs to " + home);
    }

    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.cofs = new COFs(Riiablo.assets); // TODO: not needed in prod
    Riiablo.anim = D2.loadFromFile(Riiablo.mpqs.resolve("data\\global\\eanimdata.d2"));

    if (seed == 0) {
      Gdx.app.log(TAG, "Generating seed...");
      seed = 0;
      Gdx.app.log(TAG, "seed=" + seed);
    }

    Gdx.app.log(TAG, "Generating map...");
    map = new Map(seed, diff);
    Gdx.app.log(TAG, "  generating act 1...");
    long start = TimeUtils.millis();
    map.generate(0);
    Gdx.app.log(TAG, "  act 1 generated in " + (TimeUtils.millis() - start) + "ms");

//    Gdx.app.log(TAG, "Loading act 1...");
//    Riiablo.assets.setLoader(DS1.class, new DS1Loader(Riiablo.mpqs));
//    Riiablo.assets.setLoader(DT1.class, new DT1Loader(Riiablo.mpqs));
//    map.load();
//    map.finishLoading();

    EntityFactory factory = new ServerEntityFactory();
    WorldConfigurationBuilder builder = new WorldConfigurationBuilder()
        .with(new EventSystem())
        .with(new ServerNetworkIdManager())
        .with(new CofManager())
        .with(new ObjectInitializer())

        .with(factory)
        .with(new AnimDataResolver())
        ;
    WorldConfiguration config = builder.build()
        .register("map", map)
        .register("factory", factory)
        ;
    Riiablo.engine = world = new World(config);
    world.delta = Animation.FRAME_DURATION;

    clientThreads = new ThreadGroup("D2GSClients");

    Gdx.app.log(TAG, "Starting server...");
    server = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, null);
    kill = new AtomicBoolean(false);
    connectionListener = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!kill.get()) {
          Gdx.app.log(TAG, "waiting...");
          Socket socket = server.accept(null);
          Gdx.app.log(TAG, "connection from " + socket.getRemoteAddress());
          if (CLIENTS.size() >= MAX_CLIENTS) {
            socket.dispose();
          } else {
            try {
              int id = CLIENTS.size();
              Gdx.app.log(TAG, "assigned " + id);
              Client client = new Client(id, socket);
              CLIENTS.add(client);
              client.start();
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
    connectionListener.setName("D2GS Connection Listener");
    connectionListener.start();
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "Shutting down...");
    kill.set(true);
    server.dispose();
    try {
      connectionListener.join();
    } catch (Throwable ignored) {}
    Riiablo.assets.dispose();
  }

  @Override
  public void render() {
    cache.clear();
    packets.drainTo(cache);
    for (Packet packet : cache) {
      Gdx.app.log(TAG, "processing packet from " + packet.id);
      process(packet);
    }

    world.process();

    cache.clear();
    outPackets.drainTo(cache);
    for (Packet packet : cache) {
      Gdx.app.log(TAG, "dispatching packet to " + packet.id);
      if (packet.id == -1) {
        for (Client client : CLIENTS) {
          try {
            System.out.println("  dispatching packet to " + client.id);
            if (packet.rewind) packet.data.getByteBuffer().rewind();
            client.send(packet.data);
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          }
        }
      } else {
        for (int i = 0, flag = 1; i < MAX_CLIENTS; i++, flag <<= 1) {
          if ((packet.id & flag) == flag && i < CLIENTS.size()) {
            try {
              System.out.println("  dispatching packet to " + i);
              if (packet.rewind) packet.data.getByteBuffer().rewind();
              CLIENTS.get(i).send(packet.data);
            } catch (Throwable t) {
              Gdx.app.error(TAG, t.getMessage(), t);
            }
          }
        }
      }
    }

    for (Client client : CLIENTS) {
    }
  }

  private void process(Packet packet) {
    switch (packet.data.dataType()) {
      case D2GSData.Connection:
        Connection(packet);
        break;
      default:
        Gdx.app.error(TAG, "Unknown packet type: " + packet.data.dataType());
    }
  }

  private void Connection(Packet packet) {
    Connection connection = (Connection) packet.data.data(new Connection());
    String charName = connection.charName();
    int charClass = connection.charClass();
    Gdx.app.log(TAG, "Connection from " + CLIENTS.get(packet.id).socket.getRemoteAddress() + " : " + charName);

    byte[] cofComponents = new byte[16];
    connection.cofComponentsAsByteBuffer().get(cofComponents);
    Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(cofComponents));

    float[] cofAlphas = new float[16];
    connection.cofAlphasAsByteBuffer().asFloatBuffer().get(cofAlphas);
    Gdx.app.log(TAG, "  " + Arrays.toString(cofAlphas));

    byte[] cofTransforms = new byte[16];
    connection.cofTransformsAsByteBuffer().get(cofTransforms);
    Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(cofTransforms));

    CharData charData = new CharData().createD2S(charName, CharacterClass.get(charClass));
//    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
//    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
//    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
//    Map.Zone zone = map.getZone(origin);
    Vector2 origin = new Vector2(132, 37); // FIXME: hacked for the time being
    Map.Zone zone = map.getZone(origin);
    int entityId = world.getSystem(ServerEntityFactory.class).createPlayer(map, zone, charData, origin);
    player.put(packet.id, entityId);
    Gdx.app.log(TAG, "  entityId=" + entityId);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    Connection.addEntityId(builder, entityId);
    int connectionOffset = Connection.endConnection(builder);
    int offset = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
    builder.finish(offset);
    ByteBuffer buffer = builder.dataBuffer();
    com.riiablo.net.packet.d2gs.D2GS responseData = com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(buffer);
    Packet response = Packet.obtain(1 << packet.id, responseData);
    outPackets.offer(response);

    Synchronize(packet.id, entityId);
    BroadcastConnect(packet.id, connection, charData, entityId);
  }

  private void BroadcastConnect(int id, Connection connection, CharData charData, int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int charNameOffset = builder.createString(charData.getD2S().header.name);

    byte[] components = new byte[16];
    connection.cofComponentsAsByteBuffer().get(components);
    int componentsOffset = Connection.createCofComponentsVector(builder, components);

    float[] alphas = new float[16];
    connection.cofAlphasAsByteBuffer().asFloatBuffer().get(alphas);
    int alphasOffset = Connection.createCofAlphasVector(builder, alphas);

    byte[] transforms = new byte[16];
    connection.cofTransformsAsByteBuffer().get(transforms);
    int transformsOffset = Connection.createCofTransformsVector(builder, transforms);

    Connection.startConnection(builder);
    Connection.addEntityId(builder, entityId);
    Connection.addCharClass(builder, charData.getD2S().header.charClass);
    Connection.addCharName(builder, charNameOffset);
    Connection.addCofComponents(builder, componentsOffset);
    Connection.addCofAlphas(builder, alphasOffset);
    Connection.addCofTransforms(builder, transformsOffset);
    int connectionOffset = Connection.endConnection(builder);
    int offset = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
    builder.finish(offset);
    com.riiablo.net.packet.d2gs.D2GS data = com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(builder.dataBuffer());

    Packet broadcast = Packet.obtain(~(1 << id), data);
    //broadcast.rewind = true;
    boolean success = outPackets.offer(broadcast);
    assert success;
  }

  private void Disconnect(int id) {
    int entityId = player.get(id, Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int disconnectOffset = Disconnect.createDisconnect(builder, entityId);
    int offset = com.riiablo.net.packet.d2gs.D2GS.createD2GS(builder, D2GSData.Disconnect, disconnectOffset);
    builder.finish(offset);
    ByteBuffer buffer = builder.dataBuffer();
    com.riiablo.net.packet.d2gs.D2GS responseData = com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(buffer);
    Packet broadcast = Packet.obtain(~(1 << id), responseData);
    outPackets.offer(broadcast);
  }

  private void Synchronize(int id, int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder(0);


  }

  static String generateClientName() {
    return String.format("Client-%08X", MathUtils.random(1, Integer.MAX_VALUE - 1));
  }

  private class Client extends Thread {
    int id;
    Socket socket;
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    AtomicBoolean kill = new AtomicBoolean(false);

    Client(int id, Socket socket) {
      super(clientThreads, generateClientName());
      this.id = id;
      this.socket = socket;
    }

    public void send(com.riiablo.net.packet.d2gs.D2GS data) throws IOException {
      ByteBuffer buffer = data.getByteBuffer();
      WritableByteChannel out = Channels.newChannel(socket.getOutputStream());
      out.write(buffer);
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

          ByteBuffer copy = (ByteBuffer) ByteBuffer.wrap(new byte[buffer.limit()]).put(buffer).rewind();
          com.riiablo.net.packet.d2gs.D2GS data = com.riiablo.net.packet.d2gs.D2GS.getRootAsD2GS(copy);
          Gdx.app.log(TAG, "packet type " + D2GSData.name(data.dataType()));
          boolean success = packets.offer(Packet.obtain(id, data));
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
      Disconnect(id);
    }
  }

  private static class Packet {
    int id;
    com.riiablo.net.packet.d2gs.D2GS data;
    boolean rewind = false;

    public static Packet obtain(int id, com.riiablo.net.packet.d2gs.D2GS data) {
      Packet packet = new Packet();
      packet.id = id;
      packet.data = data;
      return packet;
    }
  }
}
