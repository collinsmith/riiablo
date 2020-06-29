package com.riiablo.server.d2gs;

import io.netty.channel.ChannelFuture;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.Validate;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import net.mostlyoriginal.api.event.common.EventSystem;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.COFs;
import com.riiablo.Files;
import com.riiablo.Riiablo;
import com.riiablo.audio.ServerAudio;
import com.riiablo.codec.Animation;
import com.riiablo.codec.D2;
import com.riiablo.codec.StringTBLs;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.AIStepper;
import com.riiablo.engine.server.AnimDataResolver;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.ItemInteractor;
import com.riiablo.engine.server.ItemManager;
import com.riiablo.engine.server.ObjectInitializer;
import com.riiablo.engine.server.ObjectInteractor;
import com.riiablo.engine.server.Pathfinder;
import com.riiablo.engine.server.SerializationManager;
import com.riiablo.engine.server.ServerEntityFactory;
import com.riiablo.engine.server.ServerItemManager;
import com.riiablo.engine.server.ServerNetworkIdManager;
import com.riiablo.engine.server.VelocityAdder;
import com.riiablo.engine.server.WarpInteractor;
import com.riiablo.map.Act1MapBuilder;
import com.riiablo.map.DS1;
import com.riiablo.map.DS1Loader;
import com.riiablo.map.DT1;
import com.riiablo.map.DT1Loader;
import com.riiablo.map.Map;
import com.riiablo.map.MapManager;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.nnet.PacketProcessor;

public class Main extends ApplicationAdapter {
  private static final String TAG = "Main";

  static final int PORT = 6114;
  static final int MAX_CLIENTS = Riiablo.MAX_PLAYERS;

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

    int diff = Riiablo.NORMAL;
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
    new HeadlessApplication(new Main(home, seed, diff), config);
  }

  FileHandle home;
  int seed;
  int diff;

  Thread cli;
  AtomicBoolean kill;

  Server server;
  PacketProcessor packetProcessor;

  World world;
  Map map;

  EntityFactory factory;
  ItemManager itemManager;
  MapManager mapManager;
  NetworkSynchronizer sync;

  Main(FileHandle home, int seed, int diff) {
    this.home = home;
    this.seed = seed;
    this.diff = diff;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    final InetAddress address = getLocalHostAddress();

    logTime();
    logAddress(address);

    Riiablo.home = home = Gdx.files.absolute(home.path());
    if (!home.exists() || !home.child("d2data.mpq").exists()) {
      throw new GdxRuntimeException("home does not refer to a valid D2 installation. Copy MPQs to " + home);
    }

    Riiablo.mpqs = new MPQFileHandleResolver();
    Riiablo.assets = new AssetManager();
    Riiablo.files = new Files(Riiablo.assets);
    Riiablo.cofs = new COFs(Riiablo.assets); // TODO: not needed in prod
    Riiablo.string = new StringTBLs(Riiablo.mpqs); // TODO: not needed in prod
    Riiablo.anim = D2.loadFromFile(Riiablo.mpqs.resolve("data\\global\\eanimdata.d2"));
    Riiablo.audio = new ServerAudio(Riiablo.assets);

    // set DT1 to headless mode
    DT1.loadData = false;
    Riiablo.assets.setLoader(DS1.class, new DS1Loader(Riiablo.mpqs));
    Riiablo.assets.setLoader(DT1.class, new DT1Loader(Riiablo.mpqs));

    Riiablo.home = home = Gdx.files.absolute(home.path());
    if (!home.exists() || !home.child("d2data.mpq").exists()) {
      throw new GdxRuntimeException("home does not refer to a valid D2 installation. Copy MPQs to " + home);
    }

    if (seed == 0) {
      Gdx.app.log(TAG, "Generating seed...");
      seed = MathUtils.random.nextInt();
      Gdx.app.log(TAG, "seed=" + seed);
    }

    Gdx.app.log(TAG, "Generating map...");
    map = new Map(seed, diff);
    mapManager = new MapManager();
    Gdx.app.log(TAG, "  generating act 1...");
    long start = TimeUtils.millis();
    map.generate(0);
    Gdx.app.log(TAG, "  act 1 generated in " + (TimeUtils.millis() - start) + "ms");

    Gdx.app.log(TAG, "Loading act 1...");
    map.load();
    map.finishLoading();

    packetProcessor = new D2GSPacketProcessor();
    server = new Server(address, PORT, packetProcessor);

    factory = new ServerEntityFactory();
    itemManager = new ServerItemManager();
    mapManager = new MapManager();
    sync = new NetworkSynchronizer();
    WorldConfigurationBuilder builder = new WorldConfigurationBuilder()
        .with(new EventSystem())
        .with(new ServerNetworkIdManager())
        .with(new SerializationManager())
        .with(mapManager)
        .with(itemManager)
        .with(new CofManager())
        .with(new ObjectInitializer())
        .with(new ObjectInteractor(), new WarpInteractor(), new ItemInteractor())

        .with(new AIStepper())
        .with(new Pathfinder())

        .with(new VelocityAdder()) // FIXME: temp until proper physics implemented

        .with(factory)
        .with(sync)
        .with(new AnimDataResolver())
        ;
    WorldConfiguration config = builder.build()
        .register("map", map)
        .register("factory", factory)
        .register("player", server.player)
        .register("outPackets", server.outPackets)
        ;
    Riiablo.engine = world = new World(config);

    world.inject(map);
    world.inject(Act1MapBuilder.INSTANCE);

    map.generate();
    mapManager.createEntities();

    world.delta = Animation.FRAME_DURATION;

    kill = new AtomicBoolean(false);
    cli = createCLI();
    cli.start();

    Gdx.app.log(TAG, "Creating server instance...");
    server.create();
    Gdx.app.log(TAG, "Starting server instance...");
    ChannelFuture f = server.start();
    Gdx.app.log(TAG, "Waiting for server startup...");
    f.syncUninterruptibly();
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "Shutting down...");

    Gdx.app.log(TAG, "Notifying clients...");
    // TODO: send disconnection packets to clients

    Gdx.app.log(TAG, "Disposing world...");
    world.dispose();

    Gdx.app.log(TAG, "Disposing assets...");
    Riiablo.assets.dispose();

    Gdx.app.log(TAG, "Shutting down command-line interface...");
    try {
      kill.set(true);
      cli.interrupt();
      cli.join();
    } catch (Throwable ignored) {}

    Gdx.app.log(TAG, "Disposing server instance...");
    server.dispose();
  }

  @Override
  public void render() {
    final float delta = Gdx.graphics.getDeltaTime();
    server.update(delta);
    world.process();
    // TODO: send outgoing packets
  }

  private InetAddress getLocalHostAddress() {
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
      return null;
    }
  }

  private void logTime() {
    final Calendar calendar = Calendar.getInstance();
    DateFormat format = DateFormat.getDateTimeInstance();
    Gdx.app.log(TAG, format.format(calendar.getTime()));
  }

  private void logAddress(InetAddress address) {
    if (address == null) return;
    Gdx.app.log(TAG, "IP Address: " + address.getHostAddress() + ":" + PORT);
    Gdx.app.log(TAG, "Host Name: " + address.getHostName());
  }

  private Thread createCLI() {
    Validate.validState(cli == null);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!kill.get()) {
          try {
            if (!reader.ready()) continue;
            String in = reader.readLine();
            if (in.equalsIgnoreCase("exit")) {
              Gdx.app.exit();
            } else if (in.equalsIgnoreCase("address")) {
              Gdx.app.log(TAG, "address: " + server.future().channel().localAddress());
            } else if (in.equalsIgnoreCase("clients")) {
              Gdx.app.log(TAG, "clients: " + server.resolver());
            } else if (in.equalsIgnoreCase("seed")) {
              Gdx.app.log(TAG, "seed: " + seed);
            } else {
              Gdx.app.error(TAG, "Unknown command: " + in);
            }
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          }
        }
      }
    });
    t.setName("CLI");
    return t;
  }
}
