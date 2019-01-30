package gdx.diablo.entity3;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import gdx.diablo.COFs;
import gdx.diablo.Diablo;
import gdx.diablo.Files;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.DC6;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.StringTBLs;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.loader.DCCLoader;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class EntityTest {
  @BeforeClass
  public static void setUp() throws Exception {
    final AtomicBoolean block = new AtomicBoolean(true);
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    new HeadlessApplication(new ApplicationAdapter() {
      @Override
      public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        MPQFileHandleResolver resolver = Diablo.mpqs = new MPQFileHandleResolver();
        resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\patch_d2.mpq"));
        resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2exp.mpq"));
        resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2data.mpq"));
        resolver.add(Gdx.files.absolute("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Diablo II\\d2char.mpq"));

        Diablo.assets = new AssetManager();
        Diablo.assets.setLoader(DCC.class, new DCCLoader(Diablo.mpqs));
        Diablo.assets.setLoader(DC6.class, new DC6Loader(Diablo.mpqs));

        Diablo.cofs = new COFs(Diablo.assets);
        Diablo.files = new Files(Diablo.assets);
        Diablo.string = new StringTBLs(resolver);
        block.set(false);
      }

      @Override
      public void dispose() {
        Diablo.assets.dispose();
      }
    }, config);
    while (block.get());
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Gdx.app.exit();
  }

  @Test
  public void validateStatic() {
    Entity entity = new Entity("AF");
    entity.setMode("NU");
    entity.setWeaponClass("HTH");
    entity.validate();
  }

  @Test
  public void validateMonster() {
    Entity entity = new Entity("BH", Entity.EntType.MONSTER);
    entity.setMode("NU");
    entity.setWeaponClass("HTH");
    entity.validate();
  }

  @Test
  public void validatePlayer() {
    D2S d2s = D2S.loadFromFile(Gdx.files.local("test/Tirant.d2s"));
    Player entity = new Player(d2s);
    entity.setMode("TN");
    entity.setWeaponClass("1hs");
    entity.validate();

    entity.setSlot(Player.Slot.HEAD, null);
    entity.validate();

    entity.setMode("RN");
    entity.validate();
  }
}