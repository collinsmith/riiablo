package gdx.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import gdx.diablo.codec.FontTBL;
import gdx.diablo.loader.BitmapFontLoader;

public class Fonts {
  public final BitmapFont         consolas16;
  public final FontTBL.BitmapFont font16;
  public final FontTBL.BitmapFont font24;
  public final FontTBL.BitmapFont font30;
  public final FontTBL.BitmapFont font42;
  public final FontTBL.BitmapFont fontformal12;
  public final FontTBL.BitmapFont fontexocet10;
  public final FontTBL.BitmapFont ReallyTheLastSucker;

  public Fonts(AssetManager assets) {
    consolas16   = loadEx(assets, "consolas16.fnt");
    font16       = load(assets, "font16", BlendMode.LUMINOSITY_TINT);
    font24       = load(assets, "font24", BlendMode.ID);
    font30       = load(assets, "font30", BlendMode.ID);
    font42       = load(assets, "font42", BlendMode.ID);
    fontformal12 = load(assets, "fontformal12", BlendMode.LUMINOSITY_TINT);
    fontexocet10 = load(assets, "fontexocet10", BlendMode.TINT_BLACKS);
    ReallyTheLastSucker = load(assets, "ReallyTheLastSucker", BlendMode.ID);
  }

  private BitmapFont loadEx(AssetManager assets, String fontName) {
    assets.load(fontName, BitmapFont.class);
    assets.finishLoadingAsset(fontName);
    return assets.get(fontName);
  }

  private FontTBL.BitmapFont load(AssetManager assets, String fontName, int blendMode) {
    AssetDescriptor<FontTBL.BitmapFont> descriptor = getDescriptor(fontName, blendMode);
    assets.load(descriptor);
    assets.finishLoadingAsset(descriptor);
    return assets.get(descriptor);
  }

  private static AssetDescriptor<FontTBL.BitmapFont> getDescriptor(String fontName, int blendMode) {
    return new AssetDescriptor<>("data\\local\\font\\latin\\" + fontName + ".TBL", FontTBL.BitmapFont.class, BitmapFontLoader.Params.of(blendMode));
  }
}
