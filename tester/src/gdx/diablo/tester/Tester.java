package gdx.diablo.tester;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester1301";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));

    /*
    FileHandle obj = Gdx.files.internal("data/obj.txt");
    TXT objTXT = TXT.loadFromFile(obj, "Id", false);

    FileHandle objects = resolver.resolve("data\\global\\excel\\objects.txt");
    TXT objectsTXT = TXT.loadFromFile(objects, "Id", true);

    for (int i = 0; i < objTXT.getRows(); i++) {
      if (objTXT.getInt(i, "Act") == 5
       && objTXT.getInt(i, "Type") == 2) {
        int id = objTXT.getInt(i, "ObjectId");
        if (id >= objectsTXT.getRows() || id == -1) {
          System.out.println("Bad value: " + id);
          continue;
        }
        String desc = objectsTXT.get(id, "Name");
        System.out.println(desc);
      }
    }
    */

    Gdx.app.exit();
  }
}
