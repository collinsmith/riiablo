package com.riiablo.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.riiablo.COFs;
import com.riiablo.Colors;
import com.riiablo.Files;
import com.riiablo.Fonts;
import com.riiablo.Palettes;
import com.riiablo.Riiablo;
import com.riiablo.Textures;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.codec.FontTBL;
import com.riiablo.codec.Palette;
import com.riiablo.codec.StringTBLs;
import com.riiablo.engine.Engine;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.system.AnimationLoaderSystem;
import com.riiablo.engine.system.AnimationSystem;
import com.riiablo.engine.system.CofLoaderSystem;
import com.riiablo.engine.system.CofSystem;
import com.riiablo.engine.system.CollisionSystem;
import com.riiablo.engine.system.IdSystem;
import com.riiablo.engine.system.LabelSystem;
import com.riiablo.engine.system.ObjectSystem;
import com.riiablo.engine.system.SelectableSystem;
import com.riiablo.engine.system.SelectedSystem;
import com.riiablo.engine.system.WarpSystem;
import com.riiablo.engine.system.debug.Box2DDebugRenderSystem;
import com.riiablo.engine.system.debug.PathDebugSystem;
import com.riiablo.engine.system.debug.PathfindDebugSystem;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.loader.BitmapFontLoader;
import com.riiablo.loader.COFLoader;
import com.riiablo.loader.DC6Loader;
import com.riiablo.loader.DCCLoader;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.pfa.GraphPath;
import com.riiablo.mpq.MPQFileHandleResolver;
import com.riiablo.util.DebugUtils;

import org.apache.commons.lang3.math.NumberUtils;

public class MapViewer extends ApplicationAdapter {
  private static final String TAG = "MapViewer";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = TAG;
    config.resizable = true;
    config.vSyncEnabled = false;
    config.width = 1280; // 1280
    config.height = 720;
    config.foregroundFPS = config.backgroundFPS = 1000;
    MapViewer client = new MapViewer(args);
    new LwjglApplication(client, config);
  }

  private final String[] PALETTES = {Palettes.ACT1, Palettes.ACT2, Palettes.ACT3, Palettes.ACT4, Palettes.ACT5};

  ShapeRenderer shapes;
  Texture palette;
  Map map;

  Entity ent;
  Engine engine;
  RenderSystem mapRenderer;

  BitmapFont font;

  Box2DPhysicsSystem box2DPhysicsSystem;
  Box2DDebugRenderSystem box2DDebugRenderSystem;

  int x, y;
  final Vector2 tmpVec2a = new Vector2();
  final Vector2 tmpVec2b = new Vector2();

  final Vector2 pathStart = new Vector2();
  final Vector2 pathEnd = new Vector2();
  GraphPath generatedPath = new GraphPath();
  GraphPath smoothedPath = new GraphPath();
  PathfindDebugSystem pathfindDebugSystem;

  boolean drawCrosshair;
  boolean drawGrid;
  boolean drawFlags;
  boolean drawObjects = true;
  boolean drawWalls = true;
  boolean drawRoofs = true;
  boolean drawSpecial = true;
  boolean drawRawPathNodes = true;
  boolean drawBox2D = true;
  boolean drawDebug = true;
  final StringBuilder builder = new StringBuilder(256);

  FileHandle home;
  int seed;
  int act;
  int diff;

  MapViewer(String[] args) {
    this(args[0], NumberUtils.toInt(args[1]), NumberUtils.toInt(args[2]), NumberUtils.toInt(args[3]));
  }

  MapViewer(String home, int seed, int act, int diff) {
    this.home = new FileHandle(home);
    this.seed = seed;
    this.act = act;
    this.diff = diff;
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    Riiablo.home = home = Gdx.files.absolute(home.path());
    MPQFileHandleResolver resolver = Riiablo.mpqs = new MPQFileHandleResolver();

    AssetManager assets = Riiablo.assets = new AssetManager();
    assets.setLoader(DS1.class, new DS1Loader(resolver));
    assets.setLoader(DT1.class, new DT1Loader(resolver));
    assets.setLoader(COF.class, new COFLoader(resolver));
    assets.setLoader(DCC.class, new DCCLoader(resolver));
    assets.setLoader(DC6.class, new DC6Loader(resolver));
    assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(resolver));

    font = new BitmapFont();

    Riiablo.files = new Files(assets);
    Riiablo.fonts = new Fonts(assets);
    Riiablo.colors = new Colors();
    Riiablo.textures = new Textures();
    Riiablo.string = new StringTBLs(resolver);
    Riiablo.cofs = new COFs(assets);//COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a1.d2"));

    ShaderProgram.pedantic = false;
    ShaderProgram shader = Riiablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    PaletteIndexedBatch batch = Riiablo.batch = new PaletteIndexedBatch(2048, shader);
    shapes = new ShapeRenderer();
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);

    Riiablo.engine = engine = new Engine();

    map = new Map(seed, diff);

    RenderSystem.RENDER_DEBUG_SUBTILE = true;
    mapRenderer = new RenderSystem(batch, map);
    mapRenderer.resize();
    mapRenderer.setProcessing(false);

    box2DPhysicsSystem = new Box2DPhysicsSystem(map, mapRenderer.iso, 1 / 60f);
    box2DPhysicsSystem.setProcessing(false);

    box2DDebugRenderSystem = new Box2DDebugRenderSystem(mapRenderer);
    box2DDebugRenderSystem.setProcessing(false);

    pathfindDebugSystem = new PathfindDebugSystem(mapRenderer.iso, mapRenderer, batch, shapes);

    engine.addSystem(new IdSystem());
    engine.addSystem(box2DPhysicsSystem);
    engine.addSystem(new CofSystem());
    engine.addSystem(new CofLoaderSystem());
    engine.addSystem(new AnimationLoaderSystem());
    engine.addSystem(new AnimationSystem());
    engine.addSystem(new ObjectSystem());
    engine.addSystem(new WarpSystem(map));
    engine.addSystem(new SelectableSystem());
    engine.addSystem(new SelectedSystem(mapRenderer.iso));
    engine.addSystem(new CollisionSystem());
    engine.addSystem(mapRenderer);
    engine.addSystem(new LabelSystem(mapRenderer.iso));
    engine.addSystem(box2DDebugRenderSystem);
    engine.addSystem(new PathDebugSystem(mapRenderer.iso, mapRenderer, batch, shapes));
    engine.addSystem(pathfindDebugSystem);

    map.setAct(act);
    map.load();
    map.finishLoading();
    map.generate();
    box2DPhysicsSystem.createBodies();

    RenderSystem.RENDER_DEBUG_TILE = true;

    InputMultiplexer multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(new InputAdapter() {
      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        switch (button) {
          case Input.Buttons.RIGHT:
            pathStart.setZero();
            pathEnd.setZero();
            generatedPath.clear();
            smoothedPath.clear();
            break;
          case Input.Buttons.LEFT:
            mapRenderer.iso.agg(pathStart.set(screenX, screenY)).unproject().toWorld();
            pathEnd.setZero();
            break;
        }
        return true;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!pathStart.isZero()) mapRenderer.iso.agg(pathEnd.set(screenX, screenY)).unproject().toWorld();
        return true;
      }

      @Override
      public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (button) {
          case Input.Buttons.LEFT:
            mapRenderer.iso.agg(pathEnd.set(screenX, screenY)).unproject().toWorld();

            final int flags = Tile.FLAG_BLOCK_WALK;
            final int size = 0;
            map.findPath(pathStart, pathEnd, flags, size, generatedPath);
            smoothedPath.clear();
            smoothedPath.nodes.addAll(generatedPath.nodes);
            map.smoothPath(flags, size, smoothedPath);
            System.out.println(generatedPath + "->" + smoothedPath);
            break;
        }
        return true;
      }
    });
    multiplexer.addProcessor(new InputAdapter() {
      private final float ZOOM_AMOUNT = 0.1f;

      @Override
      public boolean scrolled(int amount) {
        switch (amount) {
          case -1:
            mapRenderer.zoom(Math.max(0.20f, mapRenderer.zoom() - ZOOM_AMOUNT));
            break;
          case 1:
            mapRenderer.zoom(Math.min(2.50f, mapRenderer.zoom() + ZOOM_AMOUNT));
            break;
          default:
        }
        return true;
      }

      @Override
      public boolean keyDown(int keycode) {
        switch (keycode) {
          case Input.Keys.TAB:
            RenderSystem.RENDER_DEBUG_GRID++;
            if (RenderSystem.RENDER_DEBUG_GRID > RenderSystem.DEBUG_GRID_MODES) {
              RenderSystem.RENDER_DEBUG_GRID = 0;
            }
            return true;
          case Input.Keys.GRAVE:
            RenderSystem.RENDER_DEBUG_WALKABLE++;
            if (RenderSystem.RENDER_DEBUG_WALKABLE > Map.MAX_LAYERS + 1) {
              RenderSystem.RENDER_DEBUG_WALKABLE = 0;
            }
            return true;
          case Input.Keys.ALT_LEFT:
            mapRenderer.resize();
            return true;
          case Input.Keys.F1:
            drawCrosshair = !drawCrosshair;
            return true;
          case Input.Keys.F2:
            drawGrid = !drawGrid;
            return true;
          case Input.Keys.F3:
            drawFlags = !drawFlags;
            return true;
          case Input.Keys.F4:
            drawObjects = !drawObjects;
            return true;
          case Input.Keys.F5:
            drawWalls = !drawWalls;
            return true;
          case Input.Keys.F6:
            drawRoofs = !drawRoofs;
            return true;
          case Input.Keys.F7:
            drawSpecial = !drawSpecial;
            return true;
          case Input.Keys.F10:
            drawRawPathNodes = !drawRawPathNodes;
            return true;
          case Input.Keys.F11:
            drawBox2D = !drawBox2D;
            return true;
          case Input.Keys.F12:
            drawDebug = !drawDebug;
            return true;
          case Input.Keys.W:
          //case Input.Keys.UP:
            int amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            y -= amount;
            ent.getComponent(PositionComponent.class).position.set(x, y);
            return true;
          case Input.Keys.S:
          //case Input.Keys.DOWN:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            y += amount;
            ent.getComponent(PositionComponent.class).position.set(x, y);
            return true;
          case Input.Keys.A:
          //case Input.Keys.LEFT:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            x -= amount;
            ent.getComponent(PositionComponent.class).position.set(x, y);
            return true;
          case Input.Keys.D:
          //case Input.Keys.RIGHT:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            x += amount;
            ent.getComponent(PositionComponent.class).position.set(x, y);
            return true;
          case Input.Keys.UP:
            Gdx.input.setCursorPosition(Gdx.input.getX(), Gdx.input.getY() - 1);
            return true;
          case Input.Keys.DOWN:
            Gdx.input.setCursorPosition(Gdx.input.getX(), Gdx.input.getY() + 1);
            return true;
          case Input.Keys.LEFT:
            Gdx.input.setCursorPosition(Gdx.input.getX() - 1, Gdx.input.getY());
            return true;
          case Input.Keys.RIGHT:
            Gdx.input.setCursorPosition(Gdx.input.getX() + 1, Gdx.input.getY());
            return true;
          default:
            return false;
        }
      }

      @Override
      public boolean mouseMoved(int screenX, int screenY) {
        //GridPoint2 pt = mapRenderer.toWorldSpace(screenX, screenY);
        //System.out.println(pt);
        return true;
      }
    });
    Gdx.input.setInputProcessor(multiplexer);

    /*
    //Diablo.dt1s = new DT1s();
    map = Map.build(0, 0, 0);
    map.load();

    mapRenderer.setMap(map);
    //mapRenderer.setFactor(1.75f);

    GridPoint2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin != null) {
      x = origin.x;
      y = origin.y;
      mapRenderer.setPosition(x, y);
    } else {
      x += DT1.Tile.SUBTILE_CENTER.x;
      y += DT1.Tile.SUBTILE_CENTER.y;
      mapRenderer.setPosition(x, y);
    }
    */

    palette = Palette.loadFromFile(Riiablo.mpqs.resolve(PALETTES[0])).render();

    /*
    for (String asset : Diablo.assets.getAssetNames()) {
      Gdx.app.debug(TAG, Diablo.assets.getReferenceCount(asset) + " : " + asset);
    }

    Gdx.app.debug(TAG, "disposing map...");
    map.dispose();

    for (String asset : Diablo.assets.getAssetNames()) {
      Gdx.app.debug(TAG, Diablo.assets.getReferenceCount(asset) + " : " + asset);
    }
    */

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin != null) {
      x = (int) origin.x;
      y = (int) origin.y;
    }

    ent = createSrc(x, y);
    mapRenderer.setSrc(ent);
    mapRenderer.updatePosition(true);

    for (String asset : Riiablo.assets.getAssetNames()) {
      Gdx.app.debug(TAG, Riiablo.assets.getReferenceCount(asset) + " : " + asset);
    }

    Gdx.app.debug(TAG, "JAVA: " + Gdx.app.getJavaHeap() + " Bytes; NATIVE: " + Gdx.app.getNativeHeap() + " Bytes");
    Riiablo.batch.setGamma(1.2f);

    //World world = new World(0, 0);
    //world.setAct(0);
    //Gdx.app.exit();
  }

  private static boolean allEqual(Map map, int x, int y, int len, int flags) {
    len += x;
    while (x < len && map.flags(x, y) == flags) x++;
    return x == len;
  }

  private Entity createSrc(float x, float y) {
    PositionComponent positionComponent = engine.createComponent(PositionComponent.class);
    positionComponent.position.set(x, y);

    Entity entity = engine.createEntity();
    entity.add(positionComponent);
    return entity;
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    Riiablo.assets.update();
    engine.update(Gdx.graphics.getDeltaTime());

    mapRenderer.iso.update();

    PaletteIndexedBatch batch = Riiablo.batch;
    batch.begin(palette);
    //batch.disableBlending();
    mapRenderer.update(Gdx.graphics.getDeltaTime());

    LabelSystem labelSystem = engine.getSystem(LabelSystem.class);
    labelSystem.update(0);
    Array<Actor> labels = labelSystem.getLabels();
    for (Actor label : labels) {
      label.draw(batch, 1);
    }

    batch.end();
    //batch.enableBlending();

    if (drawDebug) {
      shapes.identity();
      shapes.setProjectionMatrix(mapRenderer.iso.combined);
      shapes.setAutoShapeType(true);
      shapes.begin(ShapeRenderer.ShapeType.Line);
      mapRenderer.drawDebug(shapes);
      shapes.end();

      if (drawBox2D) {
        box2DDebugRenderSystem.update(0);
      }

      engine.getSystem(PathDebugSystem.class).update(0);

      if (!pathStart.isZero() && !pathEnd.isZero()) {
        pathfindDebugSystem.drawDebugPath(generatedPath, Color.RED, drawRawPathNodes);
        pathfindDebugSystem.drawDebugPath(smoothedPath, Color.GREEN, true);

        mapRenderer.iso.agg(tmpVec2a.set(pathStart)).toScreen();
        mapRenderer.iso.agg(tmpVec2b.set(pathEnd)).toScreen();
        shapes.begin();
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.WHITE);
        DebugUtils.drawEllipse2(shapes, tmpVec2a.x, tmpVec2a.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
        shapes.set(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.ORANGE);
        shapes.rectLine(tmpVec2a, tmpVec2b, 1);
        shapes.end();
      }
    }

    final int width  = Gdx.graphics.getWidth();
    final int height = Gdx.graphics.getHeight();

    if (drawCrosshair) {
      shapes.identity();
      shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
      shapes.updateMatrices();
      shapes.begin(ShapeRenderer.ShapeType.Line);
      shapes.setColor(Color.GREEN);
      shapes.line(0, height / 2, width, height / 2);
      shapes.line(width / 2, 0, width / 2, height);
      shapes.end();
    }

    batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    batch.begin();
    batch.setShader(null);

    final int textHeight = 16;
    final String SEPARATOR = "  |  ";
    builder.setLength(0);
    String details = builder
        .append("F1 crosshair").append(SEPARATOR)
        .append("F10 path nodes").append(SEPARATOR)
        .append("F11 box2d").append(SEPARATOR)
        .append("F12 debug")
        .toString();
    batch.setBlendMode(BlendMode.SOLID, Color.BLACK);
    batch.draw(Riiablo.textures.modal, 0, height - textHeight, width, textHeight);
    font.draw(batch, details, 2, height - 2);

    mapRenderer.iso.agg(tmpVec2a.set(Gdx.input.getX(), Gdx.input.getY())).unproject().toWorld().toTile();
    builder.setLength(0);
    String status = builder
        .append(Gdx.graphics.getFramesPerSecond() + " FPS").append('\n')
        .append(Gdx.app.getJavaHeap() / (1 << 20) + " MB").append('\n')
        .append("(" + (int) tmpVec2a.x + ", " + (int) tmpVec2a.y + ")").append('\n')
        .append("(" + x + ", " + y + ")").append('\n')
        .append("Grid (TAB) " + (RenderSystem.RENDER_DEBUG_GRID == 0 ? "OFF" : RenderSystem.RENDER_DEBUG_GRID)).append('\n')
        .append("Walkable (~) " + (RenderSystem.RENDER_DEBUG_WALKABLE == 0 ? "OFF" : RenderSystem.RENDER_DEBUG_WALKABLE)).append('\n')
        .append("Zoom " + String.format("%.2f", mapRenderer.zoom()))
        .toString();
    batch.setBlendMode(BlendMode.SOLID, Color.BLACK);
    batch.draw(Riiablo.textures.modal, 0, height - 140, 120, 140 - textHeight);
    font.draw(batch, status, 0, height - textHeight);

    batch.end();
    batch.setShader(Riiablo.shader);
  }

  @Override
  public void dispose() {
    font.dispose();
    shapes.dispose();
    palette.dispose();
    Riiablo.textures.dispose();
    Riiablo.assets.dispose();
    Riiablo.batch.dispose();
    Riiablo.shader.dispose();
  }
}
