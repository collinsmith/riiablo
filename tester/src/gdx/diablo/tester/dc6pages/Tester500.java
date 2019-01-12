package gdx.diablo.tester.dc6pages;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import gdx.diablo.Palettes;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.Palette;
import gdx.diablo.mpq.MPQ;

public class Tester500 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester500";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester500(), config);
  }

  Batch batch;
  ShaderProgram shader;

  Texture palette;
  DC6 dc6;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQ d2data = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    palette = Palette.loadFromStream(d2data.read(Palettes.UNITS)).render();
    dc6 = DC6.loadFromStream(d2data.read("data\\global\\ui\\FrontEnd\\WideButtonBlank.dc6"));
    dc6.loadDirection(0, true);

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(Gdx.files.internal("shaders/indexpalette.vert"), Gdx.files.internal("shaders/indexpalette.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader");
    }

    batch = new SpriteBatch();
    batch.setShader(shader);

    //Gdx.app.exit();
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    palette.bind(1);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

    batch.begin();
    shader.setUniformi("ColorTable", 1);
    shader.setUniformi("usesColormap", 0);
    batch.draw(dc6.getTexture(), 0, 0);
    batch.draw(dc6.getTexture(0), 0, 100);
    batch.draw(dc6.getTexture(1), 0, 200);
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    shader.dispose();
    palette.dispose();
    dc6.dispose();
  }
}
