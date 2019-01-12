package gdx.diablo.tester.d2s;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

import gdx.diablo.Diablo;
import gdx.diablo.Files;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester1400 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester1400";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester1400(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    MPQFileHandleResolver resolver = Diablo.mpqs = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:/Diablo II/patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:/Diablo II/d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:/Diablo II/d2data.mpq"));

    AssetManager assets = Diablo.assets = new AssetManager();
    Diablo.files = new Files(assets);
    Diablo.string = new StringTBLs(resolver);

    D2S.loadFromFile(Gdx.files.local("test/Tirant.d2s"));
    Gdx.app.exit();
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void dispose() {}
}
