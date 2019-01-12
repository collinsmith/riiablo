package gdx.diablo.map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;

import gdx.diablo.Diablo;
import gdx.diablo.Files;
import gdx.diablo.Palettes;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.COFD2;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.Palette;
import gdx.diablo.codec.TXT;
import gdx.diablo.codec.excel.Excel;
import gdx.diablo.codec.excel.LvlPrest;
import gdx.diablo.codec.excel.LvlTypes;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.loader.COFLoader;
import gdx.diablo.loader.DCCLoader;
import gdx.diablo.loader.TXTLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class DS1Viewer extends ApplicationAdapter {

  private static final String TAG = "DS1Viewer";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "DS1 Viewer";
    config.resizable = true;
    config.width = 1280;
    config.height = 720;
    config.foregroundFPS = config.backgroundFPS = 144;
    DS1Viewer client = new DS1Viewer();
    new LwjglApplication(client, config);
  }

  private final String[] PALETTES = {Palettes.ACT1, Palettes.ACT2, Palettes.ACT3, Palettes.ACT4, Palettes.ACT5};

  Stage stage;
  OrthographicCamera camera;
  AssetDescriptor<Map> mapDescriptor;
  ShapeRenderer shapes;
  Texture palette;
  Map map;
  DS1Types DS1Types;
  float renderFactor = 0.0f;//1.75f;

  int tx = 0, ty = 0;

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
    assets.setLoader(Map.class, new MapLoader(resolver));
    assets.setLoader(COF.class, new COFLoader(resolver));
    assets.setLoader(DCC.class, new DCCLoader(resolver));

    Diablo.files = new Files(assets);
    Diablo.cofs = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a1.d2"));

    TXT txt = TXT.loadFromFile(Gdx.files.local("data/ds1types.txt"));
    DS1Types = Excel.parse(txt, DS1Types.class);

    VisUI.load();

    final VisSelectBox<String> variations = new VisSelectBox<>();
    final VisSelectBox<LvlPrest.Entry> presets = new VisSelectBox<LvlPrest.Entry>() {{
      Array<LvlPrest.Entry> presets = new Array<>();
      for (LvlPrest.Entry row : Diablo.files.LvlPrest) {
        presets.add(row);
      }

      setItems(presets);
      addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          LvlPrest.Entry preset = getSelected();

          String[] files = preset.File;
          Array<String> result = new Array<>();
          for (String file : files) {
            if (file.charAt(0) != '0') result.add(file);
          }

          variations.setItems(result);
        }
      });
    }};
    variations.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        LvlPrest.Entry preset = presets.getSelected();

        String selected = variations.getSelected();
        if (mapDescriptor != null) Diablo.assets.unload(mapDescriptor.fileName);

        DS1Types.Entry ds1Type = DS1Types.get(preset.Def);
        LvlTypes.Entry lvlType = Diablo.files.LvlTypes.get(ds1Type.LevelType);

        mapDescriptor = new AssetDescriptor<>(
            "data\\global\\tiles\\" + selected + ".map", Map.class,
            new MapLoader.MapLoaderParameters(preset.Def, ds1Type.LevelType));
        Diablo.assets.load(mapDescriptor);
        Diablo.assets.finishLoadingAsset(mapDescriptor);
        System.out.println("loaded " + mapDescriptor);
        map = Diablo.assets.get(mapDescriptor);
        //DS1.Object townEntry = map.find(1, )
        int[] origin = map.find(30, 0, Orientation.SPECIAL_TILE_10);
        if (origin == null) origin = map.find(1, 0, Orientation.SPECIAL_TILE_10);
        if (origin == null) origin = map.find(1, 21, Orientation.SPECIAL_TILE_10);
        if (origin == null) origin = map.find(0, 0, Orientation.SPECIAL_TILE_11);
        if (origin == null) origin = map.find(0, 21, Orientation.SPECIAL_TILE_10);
        if (origin == null) origin = map.find(4, 4, Orientation.SPECIAL_TILE_10);

        resetCamera();
        if (origin == null) {
          tx = map.getWidth() / 2;
          ty = map.getHeight() / 2;
        } else {
          tx = origin[0];
          ty = origin[1];
        }

        // TODO: This won't work if Act doesn't match palette (act 4 lava zones in act 5)
        if (palette != null) palette.dispose();
        palette = Palette.loadFromFile(Diablo.mpqs.resolve(PALETTES[lvlType.Act - 1])).render();
      }
    });

    VisTable table = new VisTable(false);
    table.setFillParent(true);
    table.align(Align.topLeft);
    table.add(presets);
    table.add(variations);

    stage = new Stage();
    stage.addActor(table);

    camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.translate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    camera.update();

    ShaderProgram.pedantic = false;
    ShaderProgram shader = Diablo.shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette2.vert"),
        Gdx.files.internal("shaders/indexpalette2.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    Batch batch = Diablo.batch = new PaletteIndexedBatch(2048, shader);
    shapes = new ShapeRenderer();
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);

    InputMultiplexer multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(stage);
    multiplexer.addProcessor(new InputAdapter() {
      private final float ZOOM_AMOUNT = 0.1f;

      @Override
      public boolean scrolled(int amount) {
        switch (amount) {
          case -1:
            camera.zoom = Math.max(0.25f, camera.zoom - ZOOM_AMOUNT);
            break;
          case 1:
            camera.zoom = Math.min(10.0f, camera.zoom + ZOOM_AMOUNT);
            break;
          default:
        }

        camera.update();
        return true;
      }

      @Override
      public boolean keyDown(int keycode) {
        switch (keycode) {
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
          case Input.Keys.UP:
            tx--;
            if (tx < 0) tx = 0;
            ty--;
            if (ty < 0) ty = 0;
            return true;
          case Input.Keys.DOWN:
            tx++;
            if (tx > map.getWidth()) tx = map.getWidth() - 1;
            ty++;
            if (ty > map.getHeight()) ty = map.getHeight() - 1;
            return true;
          case Input.Keys.LEFT:
            tx--;
            if (tx < 0) tx = 0;
            ty++;
            if (ty > map.getHeight()) ty = map.getHeight() - 1;
            return true;
          case Input.Keys.RIGHT:
            tx++;
            if (tx > map.getWidth()) tx = map.getWidth() - 1;
            ty--;
            if (ty < 0) ty = 0;
            return true;
          case Input.Keys.FORWARD_DEL:
            resetCamera();
            return true;
          default:
            return false;
        }
      }
    });
    Gdx.input.setInputProcessor(multiplexer);
  }

  private void resetCamera() {
    camera.zoom = 1.0f;
    camera.update();
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    int width  = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();

    if (palette != null) {
      PaletteIndexedBatch batch = Diablo.batch;
      batch.setProjectionMatrix(camera.combined);
      shapes.setProjectionMatrix(camera.combined);

      batch.begin(palette);
      map.drawBackground(batch, tx, ty, 0, 0, width, height, renderFactor);
      batch.end();

      if (drawFlags) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        map.drawDebugWalkable(shapes, tx, ty, 0, 0, width, height, 1.75f);
        shapes.end();
      }

      if (drawGrid) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        map.drawDebug(shapes, tx, ty, 0, 0, width, height, 1.75f);
        shapes.end();
      }

      batch.begin(palette);
      if (drawWalls) map.drawWalls(batch, tx, ty, 0, 0, width, height, renderFactor);
      if (drawRoofs) map.drawRoofs(batch, tx, ty, 0, 0, width, height, renderFactor);
      batch.end();

      if (drawObjects) {
        batch.begin(palette);
        //map.drawEntities(entities, batch, tx, ty, 0, 0, width, height, 1.75f);
        map.drawDebugObjects(batch, tx, ty, 0, 0, width, height, 1.75f);
        batch.end();
      }

      if (drawSpecial) {
        batch.begin(palette);
        map.drawSpecial(batch, tx, ty, 0, 0, width, height, renderFactor);
        batch.end();
      }

      if (drawCrosshair) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        shapes.line(width >>> 1, 0, width >>> 1, height);
        shapes.line(0, height >>> 1, width, height >>> 1);
        shapes.end();
      }
    }

    stage.act();
    stage.draw();
  }

  @Override
  public void dispose() {
    VisUI.dispose();
    stage.dispose();
    shapes.dispose();
    palette.dispose();
    Diablo.assets.dispose();
    Diablo.batch.dispose();
    Diablo.shader.dispose();
  }
}
