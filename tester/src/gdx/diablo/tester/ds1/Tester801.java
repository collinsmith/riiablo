package gdx.diablo.tester.ds1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;

import gdx.diablo.codec.DS1;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester801 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester800";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester801(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    FileHandle home = Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II");
    MPQFileHandleResolver mpqs = new MPQFileHandleResolver();
    mpqs.add(home.child("patch_d2.mpq"));
    mpqs.add(home.child("d2exp.mpq"));
    mpqs.add(home.child("d2xmusic.mpq"));
    mpqs.add(home.child("d2xtalk.mpq"));
    mpqs.add(home.child("d2xvideo.mpq"));
    mpqs.add(home.child("d2data.mpq"));
    mpqs.add(home.child("d2char.mpq"));
    mpqs.add(home.child("d2sfx.mpq"));
    mpqs.add(home.child("d2music.mpq"));
    mpqs.add(home.child("d2speech.mpq"));
    mpqs.add(home.child("d2video.mpq"));

    int[] counter = new int[19];
    BufferedReader reader = null;
    try {
      reader = Gdx.files.internal("(listfile)").reader(4096);
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        if (FilenameUtils.isExtension(line, "ds1")) {
          FileHandle handle = mpqs.resolve(line);
          if (handle != null) {
            DS1 ds1 = DS1.loadFromStream(handle.read());
            counter[ds1.getVersion()]++;
          }
        }
      }
    } catch (IOException e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    } finally {
      StreamUtils.closeQuietly(reader);
    }

    for (int i = 1; i < counter.length; i++) {
      Gdx.app.log(TAG, "version " + i + " = " + counter[i]);
    }
    Gdx.app.exit();
  }
}
