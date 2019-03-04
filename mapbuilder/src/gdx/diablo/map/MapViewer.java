package gdx.diablo.map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.COFs;
import gdx.diablo.CharClass;
import gdx.diablo.Colors;
import gdx.diablo.Diablo;
import gdx.diablo.Files;
import gdx.diablo.Fonts;
import gdx.diablo.Palettes;
import gdx.diablo.Textures;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.codec.Palette;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.codec.TXT;
import gdx.diablo.codec.excel.Excel;
import gdx.diablo.entity.Entity;
import gdx.diablo.entity.Player;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.BitmapFontLoader;
import gdx.diablo.loader.COFLoader;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.DCCLoader;
import gdx.diablo.loader.TXTLoader;
import gdx.diablo.map.DT1.Tile;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class MapViewer extends ApplicationAdapter {

  private static final String TAG = "MapBuilder";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Map Builder";
    config.resizable = true;
    config.width = 1280; // 1280
    config.height = 720;
    config.foregroundFPS = config.backgroundFPS = 144;
    MapViewer client = new MapViewer();
    new LwjglApplication(client, config);
  }

  private final String[] PALETTES = {Palettes.ACT1, Palettes.ACT2, Palettes.ACT3, Palettes.ACT4, Palettes.ACT5};

  ShapeRenderer shapes;
  Texture palette;
  Map map;
  DS1Types DS1Types;

  Entity ent;
  MapRenderer mapRenderer;

  BitmapFont font;

  int x, y;

  Vector3 src;
  Vector3 dst;
  MapGraph.MapGraphPath path = new MapGraph.MapGraphPath();
  MapGraph.MapGraphPath smoothedPath = new MapGraph.MapGraphPath();

  boolean drawCrosshair;
  boolean drawGrid;
  boolean drawFlags;
  boolean drawObjects = true;
  boolean drawWalls = true;
  boolean drawRoofs = true;
  boolean drawSpecial = true;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    MPQFileHandleResolver resolver = Diablo.mpqs = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    AssetManager assets = Diablo.assets = new AssetManager();
    assets.setLoader(TXT.class, new TXTLoader(resolver));
    assets.setLoader(DS1.class, new DS1Loader(resolver));
    assets.setLoader(DT1.class, new DT1Loader(resolver));
    assets.setLoader(COF.class, new COFLoader(resolver));
    assets.setLoader(DCC.class, new DCCLoader(resolver));
    assets.setLoader(DC6.class, new DC6Loader(resolver));
    assets.setLoader(FontTBL.BitmapFont.class, new BitmapFontLoader(resolver));

    font = new BitmapFont();

    Diablo.files = new Files(assets);
    Diablo.fonts = new Fonts(assets);
    Diablo.colors = new Colors();
    Diablo.textures = new Textures();
    Diablo.string = new StringTBLs(resolver);
    Diablo.cofs = new COFs(assets);//COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a1.d2"));

    TXT txt = TXT.loadFromFile(Gdx.files.local("data/ds1types.txt"));
    DS1Types = Excel.parse(txt, DS1Types.class);

    ShaderProgram.pedantic = false;
    ShaderProgram shader = Diablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette3.vert"),
        Gdx.files.internal("shaders/indexpalette3.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    PaletteIndexedBatch batch = Diablo.batch = new PaletteIndexedBatch(2048, shader);
    shapes = new ShapeRenderer();
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);

    mapRenderer = new MapRenderer(batch);
    mapRenderer.resize();

    MapRenderer.RENDER_DEBUG_TILE = true;
    MapRenderer.RENDER_DEBUG_PATHS = true;

    InputMultiplexer multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(new InputAdapter() {
      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        switch (button) {
          case Input.Buttons.LEFT:
            GridPoint2 srcCoords = mapRenderer.coords();
            src = new Vector3(srcCoords.x, srcCoords.y, 0);
            dst = null;
            break;
          case Input.Buttons.RIGHT:
            src = dst = null;
            break;
        }
        return true;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int button) {
        if (src != null) {
          GridPoint2 dstCoords = mapRenderer.coords();
          dst = new Vector3(dstCoords.x, dstCoords.y, 0);
        }
        return true;
      }

      @Override
      public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (button) {
          case Input.Buttons.LEFT:
            GridPoint2 dstCoords = mapRenderer.coords();
            dst = new Vector3(dstCoords.x, dstCoords.y, 0);
            map.findPath(src, dst, path);
            smoothedPath.nodes.clear();
            smoothedPath.nodes.addAll(path.nodes);
            map.smoothPath(smoothedPath);
            System.out.println(path + "->" + smoothedPath);
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
            mapRenderer.zoom(Math.max(0.25f, mapRenderer.zoom() - ZOOM_AMOUNT));
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
            MapRenderer.RENDER_DEBUG_GRID++;
            if (MapRenderer.RENDER_DEBUG_GRID > MapRenderer.DEBUG_GRID_MODES) {
              MapRenderer.RENDER_DEBUG_GRID = 0;
            }
            return true;
          case Input.Keys.GRAVE:
            MapRenderer.RENDER_DEBUG_WALKABLE++;
            if (MapRenderer.RENDER_DEBUG_WALKABLE > Map.MAX_LAYERS + 1) {
              MapRenderer.RENDER_DEBUG_WALKABLE = 0;
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
          case Input.Keys.W:
          //case Input.Keys.UP:
            int amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            y -= amount;
            ent.position().set(x, y, 0);
            return true;
          case Input.Keys.S:
          //case Input.Keys.DOWN:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            y += amount;
            ent.position().set(x, y, 0);
            return true;
          case Input.Keys.A:
          //case Input.Keys.LEFT:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            x -= amount;
            ent.position().set(x, y, 0);
            return true;
          case Input.Keys.D:
          //case Input.Keys.RIGHT:
            amount = UIUtils.ctrl() ? 1 : Tile.SUBTILE_SIZE;
            if (UIUtils.shift()) amount *= 8;
            x += amount;
            ent.position().set(x, y, 0);
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

    palette = Palette.loadFromFile(Diablo.mpqs.resolve(PALETTES[0])).render();

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

    assets.setLoader(Map.class, new MapLoader(resolver));
    assets.load("Act 1", Map.class, MapLoader.MapParameters.of(0, 0, 0));
    assets.finishLoading();

    map = assets.get("Act 1");
    mapRenderer.setMap(map);

    GridPoint2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin != null) {
      x = origin.x;
      y = origin.y;
    }

    ent = new Player("null", CharClass.BARBARIAN);
    ent.position().set(x, y, 0);
    mapRenderer.setSrc(ent);

    for (String asset : Diablo.assets.getAssetNames()) {
      Gdx.app.debug(TAG, Diablo.assets.getReferenceCount(asset) + " : " + asset);
    }

    Gdx.app.debug(TAG, "JAVA: " + Gdx.app.getJavaHeap() + " Bytes; NATIVE: " + Gdx.app.getNativeHeap() + " Bytes");
    Diablo.batch.setGamma(1.2f);
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    mapRenderer.update();

    PaletteIndexedBatch batch = Diablo.batch;
    batch.begin(palette);
    mapRenderer.draw(Gdx.graphics.getDeltaTime());
    batch.end();

    shapes.setAutoShapeType(true);
    shapes.begin(ShapeRenderer.ShapeType.Line);
    mapRenderer.drawDebug(shapes);
    if (src != null && dst != null) {
      mapRenderer.drawDebugPath(shapes, path);
      mapRenderer.drawDebugPath(shapes, smoothedPath, Color.GREEN);
      float srcX = +(src.x * Tile.SUBTILE_WIDTH50)  - (src.y * Tile.SUBTILE_WIDTH50);
      float srcY = -(src.x * Tile.SUBTILE_HEIGHT50) - (src.y * Tile.SUBTILE_HEIGHT50);
      float dstX = +(dst.x * Tile.SUBTILE_WIDTH50)  - (dst.y * Tile.SUBTILE_WIDTH50);
      float dstY = -(dst.x * Tile.SUBTILE_HEIGHT50) - (dst.y * Tile.SUBTILE_HEIGHT50);
      shapes.setColor(Color.WHITE);
      shapes.circle(srcX, srcY, 32);
      shapes.set(ShapeRenderer.ShapeType.Filled);
      shapes.setColor(Color.ORANGE);
      shapes.rectLine(srcX, srcY, dstX, dstY, 1);
    }
    shapes.end();

    final int width  = Gdx.graphics.getWidth();
    final int height = Gdx.graphics.getHeight();

    if (drawCrosshair) {
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

    String str = new StringBuilder()
        .append(Gdx.graphics.getFramesPerSecond() + " FPS").append('\n')
        .append(Gdx.app.getJavaHeap() / (1 << 20) + " MB").append('\n')
        .append(Gdx.input.getX() + ", " + Gdx.input.getY()).append('\n')
        .append("(" + x + ", " + y + ")").append('\n')
        .append("Grid (TAB) " + (MapRenderer.RENDER_DEBUG_GRID == 0 ? "OFF" : MapRenderer.RENDER_DEBUG_GRID)).append('\n')
        .append("Walkable (~) " + (MapRenderer.RENDER_DEBUG_WALKABLE == 0 ? "OFF" : MapRenderer.RENDER_DEBUG_WALKABLE))
        .toString();
    font.draw(batch, str, 0, height);

    batch.end();
    batch.setShader(Diablo.shader);
  }

  @Override
  public void dispose() {
    font.dispose();
    shapes.dispose();
    palette.dispose();
    Diablo.textures.dispose();
    Diablo.assets.dispose();
    Diablo.batch.dispose();
    Diablo.shader.dispose();
  }
}
