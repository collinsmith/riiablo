package com.riiablo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import com.riiablo.camera.OrthographicCamera;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.map2.DS1;
import com.riiablo.map2.DS1Reader;
import com.riiablo.map2.DT1;
import com.riiablo.map2.DT1Reader;
import com.riiablo.map2.Map;
import com.riiablo.map2.MapGenerator;
import com.riiablo.map2.TileGenerator;
import com.riiablo.map2.util.DebugMode;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.tool.LwjglTool;
import com.riiablo.tool.Tool;
import com.riiablo.util.InstallationFinder;

import static com.badlogic.gdx.Input.Keys.ESCAPE;
import static com.badlogic.gdx.Input.Keys.GRAVE;
import static com.riiablo.map2.util.DebugMode.TILE;

public class MapDebugger extends Tool {
  private static final Logger log = LogManager.getLogger(MapDebugger.class);

  public static void main(String[] args) {
    LwjglTool.create(MapDebugger.class, "map-debugger", args)
        .title("Map Debugger")
        .size(800, 600)
        .start();
  }

  @Override
  protected void createCliOptions(Options options) {
    super.createCliOptions(options);

    options.addOption(Option
        .builder("d")
        .longOpt("d2")
        .desc("directory containing D2 MPQ files")
        .hasArg()
        .argName("path")
        .build());
  }

  @Override
  protected void handleCliOptions(String cmd, Options options, CommandLine cli) {
    super.handleCliOptions(cmd, options, cli);

    final InstallationFinder finder = InstallationFinder.getInstance();

    final FileHandle d2Home;
    if (cli.hasOption("d2")) {
      d2Home = new FileHandle(cli.getOptionValue("d2"));
      if (!InstallationFinder.isD2Home(d2Home)) {
        throw new GdxRuntimeException("'d2' does not refer to a valid D2 installation: " + d2Home);
      }
    } else {
      log.trace("Locating D2 installations...");
      Array<FileHandle> homeDirs = finder.getHomeDirs();
      log.trace("D2 installations: {}", homeDirs);
      if (homeDirs.size > 0) {
        d2Home = homeDirs.first();
      } else {
        System.err.println("Unable to locate any D2 installation!");
        printHelp(cmd, options);
        System.exit(0);
        return;
      }
    }
    log.debug("d2Home: {}", d2Home);
    Riiablo.home = d2Home;
  }

  OrthographicCamera ui;
  BitmapFont font;

  OrthographicCamera camera;
  Batch batch;
  Map map;
  DebugMode debugMode = TILE;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    LogManager.setLevel(MapDebugger.class.getName(), Level.DEBUG);
    LogManager.setLevel("com.riiablo.map2", Level.DEBUG);

    Riiablo.home = Gdx.files.absolute(Riiablo.home.path());
    Riiablo.mpqs = new MPQFileHandleResolver();

    ui = new OrthographicCamera();
    font = new BitmapFont();

    camera = new OrthographicCamera();
    batch = new SpriteBatch();
    Gdx.input.setInputProcessor(new InputAdapter() {
      Vector2 start = new Vector2();
      Vector2 end = new Vector2();
      Vector2 tmp = new Vector2();

      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        start.set(screenX, screenY);
        camera.unproject(start);
        return true;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int pointer) {
        end.set(screenX, screenY);
        camera.unproject(end);

        tmp.set(start).sub(end);
        camera.translate(tmp);
        camera.update();
        return true;
      }

      @Override
      public boolean scrolled(float amountX, float amountY) {
        camera.zoom += (amountY * 0.1f);
        if (camera.zoom <= 0.1f) camera.zoom = 0.1f;
        else if (camera.zoom >= 10f) camera.zoom = 10f;
        camera.update();
        return true;
      }

      @Override
      public boolean keyDown(int keycode) {
        switch (keycode) {
          case GRAVE:
            debugMode = debugMode.next;
            return true;
          case ESCAPE:
            camera.zoom = 1.0f;
            camera.setToOrtho(false); // calls camera#update()
            camera.position.set(0, 0, 0);
            camera.update(); // required due to camera.position.set(Vector3) call
            return true;
          default:
            return false;
        }
      }
    });

    DS1Reader ds1Reader = new DS1Reader();
    DS1 townN1 = readDs1(ds1Reader, "data\\global\\tiles\\act1\\town\\townn1.ds1");

    DT1Reader dt1Reader = new DT1Reader();
    MapGenerator generator = new MapGenerator();
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\town\\floor.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\town\\objects.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\town\\fence.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\outdoors\\river.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\outdoors\\stonewall.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\town\\trees.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\outdoors\\objects.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\outdoors\\treegroups.dt1");
    addDt1(generator.tileGenerator, dt1Reader, "data\\global\\tiles\\act1\\outdoors\\bridge.dt1");
    map = generator.generate(townN1);
    map.zones().claim("BloodMoor", -120, -400, 400, 400, 8, 8); // to test layout

    // map = new Map();
    // map.zones().claim("TownN1", 0, 0, 280, 200, 1, 1);
    // map.zones().claim("BloodMoor", -120, -400, 400, 400, 8, 8);
  }

  DS1 readDs1(DS1Reader ds1Reader, String fileName) {
    byte[] bytes = Riiablo.mpqs.resolve(fileName).readBytes();
    ByteInput in = ByteInput.wrap(bytes);
    return ds1Reader.readDs1(fileName, in);
  }

  DT1 addDt1(TileGenerator generator, DT1Reader dt1Reader, String fileName) {
    byte[] bytes = Riiablo.mpqs.resolve(fileName).readBytes();
    ByteInput in = ByteInput.wrap(bytes);
    DT1 dt1 = dt1Reader.readDt1(fileName, in);
    generator.add(dt1);
    return dt1;
  }

  @Override
  public void resize(int width, int height) {
    Vector3 tmp = camera.position.cpy();
    camera.setToOrtho(false, width, height); // y-down to make north-up
    camera.position.set(tmp);
    camera.update(); // required due to camera.position.set(Vector3) call
    ui.setToOrtho(false, width, height);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin();

    batch.setProjectionMatrix(camera.combined);
    map.zones().drawDebug(batch, debugMode);

    batch.setProjectionMatrix(ui.combined);
    font.draw(
        batch,
        "FPS: " + Gdx.graphics.getFramesPerSecond() + "\nDebugMode: " + debugMode,
        0,
        camera.viewportHeight);

    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    map.dispose();
    font.dispose();
  }
}
