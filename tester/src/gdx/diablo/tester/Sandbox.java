package gdx.diablo.tester;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import gdx.diablo.codec.TXT;
import gdx.diablo.codec.excel.Excel;
import gdx.diablo.codec.excel.MonMode;
import gdx.diablo.mpq.MPQFileHandleResolver;

public class Sandbox extends ApplicationAdapter {
  private static final String TAG = "Sandbox";

  public static void main(String[] args) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.title = "Sandbox";
    config.resizable = true;
    config.width = 640;
    config.height = 480;
    config.foregroundFPS = config.backgroundFPS = 144;
    new LwjglApplication(new Sandbox(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    MPQFileHandleResolver resolver = new MPQFileHandleResolver();
    resolver.add(Gdx.files.absolute("C:/Diablo II/patch_d2.mpq"));
    resolver.add(Gdx.files.absolute("C:/Diablo II/d2exp.mpq"));
    resolver.add(Gdx.files.absolute("C:/Diablo II/d2data.mpq"));

    //TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\UniqueItems.txt"));
    //UniqueItems items = Excel.parse(txt, UniqueItems.class, ObjectSet.with("Expansion"));

    //TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\bodylocs.txt"));
    //BodyLocs bodylocs = Excel.parse(txt, BodyLocs.class);

    //TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\MagicPrefix.txt"));
    //MagicSuffix MagicPrefix = Excel.parse(txt, MagicSuffix.class, Excel.EXPANSION);

    //TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\MagicSuffix.txt"));
    //MagicSuffix MagicSuffix = Excel.parse(txt, MagicSuffix.class, Excel.EXPANSION);

    //TXT txt1 = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\RarePrefix.txt"));
    //RarePrefix RarePrefix = Excel.parse(txt1, RarePrefix.class);

    //TXT txt2 = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\RareSuffix.txt"));
    //RareSuffix RareSuffix = Excel.parse(txt2, RareSuffix.class);

    //System.out.println(RarePrefix.get(169));
    //System.out.println(RareSuffix.get(138));

    //TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\Runes.txt"));
    //Runes Runes = Excel.parse(txt, Runes.class);

    /*
    TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\ItemStatCost.txt"));
    ItemStatCost ItemStatCost = Excel.parse(txt, ItemStatCost.class);

    //int length = Integer.MIN_VALUE;
    int i = 0;
    for (ItemStatCost.Entry stat : ItemStatCost) {
      assert i == stat.ID;
      i++;
      System.out.printf("%s,%n", stat.Stat);
      //System.out.printf("public static final int %31s = %d;%n", stat.Stat, stat.ID);
      //length = Math.max(length, stat.Stat.length());
    }

    //System.out.println(length);
    */

    TXT txt = TXT.loadFromFile(resolver.resolve("data\\global\\excel\\monmode.txt"));
    MonMode objMode = Excel.parse(txt, MonMode.class);

    Gdx.app.exit();
  }
}
