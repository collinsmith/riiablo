package gdx.diablo.tester.charbutton;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import gdx.diablo.CharClass;
import gdx.diablo.Diablo;
import gdx.diablo.Palettes;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.Palette;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.PaletteLoader;
import gdx.diablo.mpq.MPQ;
import gdx.diablo.mpq.MPQFileHandleResolver;
import gdx.diablo.widget.CharButton;

public class Tester600 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester600";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester600(), config);
  }

  Texture palette;
  CharButton charButton;
  Stage stage;

  Batch batch;
  ShaderProgram shader;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQ d2data = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    MPQ d2sfx  = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2sfx.mpq"));
    MPQ d2exp  = MPQ.loadFromFile(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));

    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(d2data);
    resolver.add(d2sfx);
    resolver.add(d2exp);

    Diablo.assets = new AssetManager();
    Diablo.assets.setLoader(Sound.class, new SoundLoader(resolver));
    Diablo.assets.setLoader(DC6.class, new DC6Loader(resolver));
    Diablo.assets.setLoader(Palette.class, new PaletteLoader(resolver));
    //Diablo.assets.setLoader(Animation.class, new AnimationLoader(resolver));

    charButton = new CharButton(CharClass.BARBARIAN);
    charButton.setPosition(100, 100);

    //DC6 dc6 = DC6.loadFromStream(d2exp.read("data\\global\\ui\\FrontEnd\\druid\\dzfw.DC6"));

    palette = Palette.loadFromStream(d2data.read(Palettes.FECHAR)).render();

    ShaderProgram.pedantic = false;
    shader = new ShaderProgram(Gdx.files.internal("shaders/indexpalette.vert"), Gdx.files.internal("shaders/indexpalette.frag"));
    if (!shader.isCompiled()) {
      throw new GdxRuntimeException("Error compiling shader: \n" + shader.getLog());
    }

    batch = new SpriteBatch(1024, shader);
    stage = new Stage(new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()), batch);
    stage.addActor(charButton);

    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    palette.bind(1);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

    batch.begin();

    shader.setUniformi("ColorTable", 1);

    batch.end();

    stage.act();
    stage.draw();
  }

  @Override
  public void dispose() {
    stage.dispose();
    batch.dispose();
    shader.dispose();
    palette.dispose();
    charButton.dispose();
  }
}
