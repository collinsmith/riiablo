package gdx.diablo.tester.dt1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.Palettes;
import gdx.diablo.codec.DT1;
import gdx.diablo.codec.Palette;
import gdx.diablo.mpq.MPQ;

public class Tester703 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester701";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester703(), config);
  }

  Texture palette;
  Texture tiles[];
  PaletteIndexedBatch batch;
  ShapeRenderer shapes;
  ShaderProgram shader;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQ d2exp = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    DT1 dt1 = DT1.loadFromStream("data\\global\\tiles\\ACT1\\TOWN\\fence.dt1",
        d2exp.read("data/global/tiles/Expansion/Town/trees.dt1"));

    tiles = new Texture[4];
    for (int i = 0; i < tiles.length; i++) {
      tiles[i] = new Texture(new PixmapTextureData(dt1.tile(i), null, false, true, false));
    }

    palette = Palette.loadFromStream(d2exp.read(Palettes.ACT5)).render();

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette.vert"),
        Gdx.files.internal("shaders/indexpalette.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    batch = new PaletteIndexedBatch(512, shader);
    shapes = new ShapeRenderer();

    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    int x = 128;
    int y = 100;
    batch.begin(palette);
    for (int i = 0; i < tiles.length; i++) batch.draw(tiles[i], x * i, y);
    batch.end();

    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.setColor(Color.GREEN);
    for (int i = 0; i < tiles.length; i++) shapes.rect(x * i, y, tiles[i].getWidth(), tiles[i].getHeight());
    shapes.end();
  }

  @Override
  public void dispose() {
    for (Texture tile : tiles) tile.dispose();
    palette.dispose();
    batch.dispose();
    shader.dispose();
  }
}
