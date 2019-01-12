package gdx.diablo.tester.ds1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.Palettes;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.Palette;
import gdx.diablo.codec.TXT;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.COFLoader;
import gdx.diablo.loader.DCCLoader;
import gdx.diablo.loader.TXTLoader;
import gdx.diablo.map.DS1;
import gdx.diablo.map.DS1Loader;
import gdx.diablo.map.DT1;
import gdx.diablo.map.DT1Loader;
import gdx.diablo.map.Map;
import gdx.diablo.map.MapLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester807 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester807";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester807(), config);
  }

  AssetManager assets;
  PaletteIndexedBatch batch;
  ShaderProgram shader;
  ShapeRenderer shapes;
  Texture palette;
  Map map;
  MPQFileHandleResolver resolver;

  boolean drawCrosshair;
  boolean drawGrid;
  boolean drawFlags;
  boolean drawObjects = true;
  boolean drawWalls = true;
  boolean drawRoofs = true;
  boolean drawSpecial = true;

  int tx = 30, ty = 13;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    assets = new AssetManager(resolver);
    assets.setLoader(TXT.class, new TXTLoader(resolver));
    assets.setLoader(DS1.class, new DS1Loader(resolver));
    assets.setLoader(DT1.class, new DT1Loader(resolver));
    assets.setLoader(Map.class, new MapLoader(resolver));
    assets.setLoader(COF.class, new COFLoader(resolver));
    assets.setLoader(DCC.class, new DCCLoader(resolver));


    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette2.vert"),
        Gdx.files.internal("shaders/indexpalette2.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    palette = Palette.loadFromStream(resolver.resolve(Palettes.ACT5).read()).render();
    batch = new PaletteIndexedBatch(2048, shader);
    shapes = new ShapeRenderer();
    Gdx.gl.glClearColor(0.0f, 0.0f, 1.0f, 1);

    // FIXME: This is causing problems with \ in paths, use / instead for now
    String path = "data\\global\\objects\\b6\\TR\\b6TRLITNUhth.dcc";
    AssetDescriptor<DCC> assetDescriptor = new AssetDescriptor<>(path, DCC.class);
    assets.load(assetDescriptor);
    assets.finishLoading();
    System.out.println(assets.getAssetNames());
    DCC dcc = assets.get(assetDescriptor);
    System.out.println("dcc " + dcc);
    dcc.dispose();
    Gdx.app.exit();
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

  }

  @Override
  public void dispose() {
    palette.dispose();
    batch.dispose();
    shader.dispose();
    assets.dispose();
    shapes.dispose();
  }
}
