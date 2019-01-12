package gdx.diablo.tester.ds1;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import gdx.diablo.codec.DS1;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Tester800 extends ApplicationAdapter {
  private static final String TAG = "Tester";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Tester800";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Tester800(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
    //DS1.loadFromStream(d2data.read("data\\global\\tiles\\ACT1\\TOWN\\townE1.ds1"));
    //DS1.loadFromStream(d2data.read("data\\global\\tiles\\ACT1\\tristram\\tri_town4.ds1"));
    //DS1.loadFromStream(d2data.read("DATA\\GLOBAL\\TILES\\Act4\\Fort\\Fortress.ds1"));

    FileHandle LvlPrest = resolver.resolve("data\\global\\excel\\LvlPrest.txt");
    FileHandle LvlTypes = resolver.resolve("data\\global\\excel\\LvlTypes.txt");
    //TXT.loadFromStream(LvlPrest.read(), "Def"), 863, TXT.loadFromStream(LvlTypes.read(), "Id"), 29

    FileHandle townWest = resolver.resolve("data\\global\\tiles\\expansion\\Town\\townWest.ds1");
    DS1 ds1 = DS1.loadFromStream(townWest.read());
    Gdx.app.debug(TAG, ds1.toString());
    Gdx.app.exit();
  }
}
