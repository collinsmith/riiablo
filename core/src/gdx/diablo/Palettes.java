package gdx.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.codec.Palette;

public class Palettes implements Disposable {
  public static final String ACT1      = "data\\global\\palette\\ACT1\\pal.dat";
  public static final String ACT2      = "data\\global\\palette\\ACT2\\pal.dat";
  public static final String ACT3      = "data\\global\\palette\\ACT3\\pal.dat";
  public static final String ACT4      = "data\\global\\palette\\ACT4\\pal.dat";
  public static final String ACT5      = "data\\global\\palette\\ACT5\\pal.dat";
  public static final String ENDGAME   = "data\\global\\palette\\ENDGAME\\pal.dat";
  public static final String ENDGAME2  = "data\\global\\palette\\ENDGAME2\\pal.dat";
  public static final String FECHAR    = "data\\global\\palette\\FECHAR\\pal.dat";
  public static final String LOADING   = "data\\global\\palette\\LOADING\\pal.dat";
  public static final String MENU0     = "data\\global\\palette\\MENU0\\pal.dat";
  public static final String MENU1     = "data\\global\\palette\\MENU1\\pal.dat";
  public static final String MENU2     = "data\\global\\palette\\MENU2\\pal.dat";
  public static final String MENU3     = "data\\global\\palette\\MENU3\\pal.dat";
  public static final String MENU4     = "data\\global\\palette\\MENU4\\pal.dat";
  public static final String SKY       = "data\\global\\palette\\SKY\\pal.dat";
  public static final String STATIC    = "data\\global\\palette\\STATIC\\pal.dat";
  public static final String TRADEMARK = "data\\global\\palette\\TRADEMARK\\pal.dat";
  public static final String UNITS     = "data\\global\\palette\\UNITS\\pal.dat";

  public final Texture act1, act2, act3, act4, act5;
  public final Texture endgame, endgame2;
  public final Texture fechar;
  public final Texture loading;
  public final Texture menu0, menu1, menu2, menu3, menu4;
  public final Texture sky, static0, trademark, units;

  public Palettes(AssetManager assets) {
    act1      = load(assets, "ACT1").render();
    act2      = load(assets, "ACT2").render();
    act3      = load(assets, "ACT3").render();
    act4      = load(assets, "ACT4").render();
    act5      = load(assets, "ACT5").render();
    endgame   = load(assets, "ENDGAME").render();
    endgame2  = load(assets, "ENDGAME2").render();
    fechar    = load(assets, "FECHAR").render();
    loading   = load(assets, "LOADING").render();
    menu0     = load(assets, "MENU0").render();
    menu1     = load(assets, "MENU1").render();
    menu2     = load(assets, "MENU2").render();
    menu3     = load(assets, "MENU3").render();
    menu4     = load(assets, "MENU4").render();
    sky       = load(assets, "SKY").render();
    static0   = load(assets, "STATIC").render();
    trademark = load(assets, "TRADEMARK").render();
    units     = load(assets, "UNITS").render();
  }

  private Palette load(AssetManager assets, String fontName) {
    AssetDescriptor<Palette> descriptor = getDescriptor(fontName);
    assets.load(descriptor);
    assets.finishLoadingAsset(descriptor);
    return assets.get(descriptor);
  }

  private static AssetDescriptor<Palette> getDescriptor(String paletteName) {
    return new AssetDescriptor<>("data\\global\\palette\\" + paletteName + "\\pal.dat", Palette.class);
  }

  @Override
  public void dispose() {
    act1.dispose();
    act2.dispose();
    act3.dispose();
    act4.dispose();
    act5.dispose();
    endgame.dispose();
    endgame2.dispose();
    fechar.dispose();
    loading.dispose();
    menu0.dispose();
    menu1.dispose();
    menu2.dispose();
    menu3.dispose();
    menu4.dispose();
    sky.dispose();
    static0.dispose();
    trademark.dispose();
    units.dispose();
  }
}
