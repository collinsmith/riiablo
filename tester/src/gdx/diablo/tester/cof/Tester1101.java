package gdx.diablo.tester.cof;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.Palettes;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.Palette;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.mpq.MPQ;

public class Tester1101 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester1101";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester1101(), config);
  }

  PaletteIndexedBatch batch;
  ShaderProgram shader;
  Texture palette;
  Animation animation;
  ShapeRenderer shapes;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQ d2data = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    COF cof = COF.loadFromStream(d2data.read("data\\global\\objects\\TO\\COF\\toonhth.cof"));
    animation = Animation.newAnimation(cof);

    palette = Palette.loadFromStream(d2data.read(Palettes.ACT1)).render();

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(
        Gdx.files.internal("shaders/indexpalette2.vert"),
        Gdx.files.internal("shaders/indexpalette2.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
    }

    batch = new PaletteIndexedBatch(2048, shader);
    shapes = new ShapeRenderer();

    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
  }

  @Override
  public void render() {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.begin(palette);
    animation.act();
    animation.draw(batch, 200, 200);
    batch.end();

    shapes.begin(ShapeRenderer.ShapeType.Line);
    shapes.rect(200, 200, animation.getMinWidth(), animation.getMinHeight());
    shapes.end();
  }

  @Override
  public void dispose() {
    shapes.dispose();
    shader.dispose();
    batch.dispose();
    palette.dispose();
  }
}
