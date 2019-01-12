package gdx.diablo.tester.dc6sheets;

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

import gdx.diablo.PL2s;
import gdx.diablo.Palettes;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.PL2;
import gdx.diablo.codec.Palette;
import gdx.diablo.mpq.MPQ;

public class Tester201 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester201";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester201(), config);
  }

  Palette palette;
  PL2 pl2;

  Batch batch;
  ShaderProgram shader;

  Texture colorTable;
  Texture colorMap;
  Animation animation;
  DC6 dc6;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQ d2data = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    //DC6 dc6 = DC6.loadFromStream(d2data.read("data\\global\\ui\\FrontEnd\\necromancer\\NEFW.DC6"));
    dc6 = DC6.loadFromStream(d2data.read("data\\global\\ui\\FrontEnd\\barbarian\\baFW.DC6"));

    //MPQ d2exp = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    //DC6 dc6 = DC6.loadFromStream(d2exp.read("data\\global\\ui\\FrontEnd\\druid\\dzfw.DC6"));

    palette = Palette.loadFromStream(d2data.read(Palettes.FECHAR));
    colorTable = palette.render();
    pl2 = PL2.loadFromStream(d2data.read(PL2s.FECHAR));
    colorMap = pl2.render();

    animation = Animation.newAnimation(dc6);

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(Gdx.files.internal("shaders/indexpalette.vert"), Gdx.files.internal("shaders/indexpalette.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: \n" + shader.getLog());
    }

    batch = new SpriteBatch(1024, shader);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    colorTable.bind(1);
    colorMap.bind(2);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

    batch.begin();

    shader.setUniformi("colormapIndex", 32);
    shader.setUniformi("ColorTable", 1);
    shader.setUniformi("ColorMap", 2);

    animation.act();
    animation.draw(batch, 200, 200);

    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    shader.dispose();
    colorTable.dispose();
    colorMap.dispose();
    dc6.dispose();
  }
}
