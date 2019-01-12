package gdx.diablo.tester.d2;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

import gdx.diablo.codec.COFD2;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester1203 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester1203";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester1203(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));

    // !!! commented out files are not used by game? !!!
    COFD2 chars_cof = COFD2.loadFromFile(resolver.resolve("data\\global\\chars_cof.d2"));
    COFD2 cmncof_a1 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a1.d2"));
    COFD2 cmncof_a2 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a2.d2"));
    COFD2 cmncof_a3 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a3.d2"));
    COFD2 cmncof_a4 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a4.d2"));
    //COFD2 cmncof_a5 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a5.d2"));
    COFD2 cmncof_a6 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a6.d2"));
    //COFD2 cmncof_a7 = COFD2.loadFromFile(resolver.resolve("data\\global\\cmncof_a7.d2"));
    System.out.println(chars_cof.getNumEntries() + " records");
    System.out.println(cmncof_a1.getNumEntries() + " records");
    System.out.println(cmncof_a2.getNumEntries() + " records");
    System.out.println(cmncof_a3.getNumEntries() + " records");
    System.out.println(cmncof_a4.getNumEntries() + " records");
    //System.out.println(cmncof_a5.getNumEntries() + " records");
    System.out.println(cmncof_a6.getNumEntries() + " records");
    //System.out.println(cmncof_a7.getNumEntries() + " records");
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
