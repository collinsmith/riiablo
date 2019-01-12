package gdx.diablo.tester.audio;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

public class Tester301 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester301";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester301(), config);
  }

  @Override
  public void create() {
    FileHandle listfile = Gdx.files.internal("(listfile)");
    FileHandle listfile0 = new FileHandle(Gdx.files.getLocalStoragePath()).child("(listfile).0");

    BufferedReader reader = null;
    Writer writer = null;
    try {
      String line;
      reader = listfile.reader(4096, "US-ASCII");
      writer = listfile0.writer(false, "US-ASCII");
      while ((line = reader.readLine()) != null) {
        if (line.endsWith(".wav")) {
          System.out.println(line);
          writer.write(line);
          writer.write('\n');
        }
      }
    } catch (IOException e) {
      throw new GdxRuntimeException("Couldn't read " + listfile, e);
    } finally {
      StreamUtils.closeQuietly(reader);
      StreamUtils.closeQuietly(writer);
    }

    Gdx.app.exit();
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void dispose() {
  }
}
