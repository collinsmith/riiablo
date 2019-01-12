package gdx.diablo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.codec.Index;

public class Colormaps implements Disposable {
  public final Index brown, gold;
  public final Index grey, grey2, greybrown;
  public final Index invgrey, invgrey2, invgreybrown;

  public Colormaps(AssetManager assets) {
    brown        = load(assets, "brown").render();
    gold         = load(assets, "gold").render();
    grey         = load(assets, "grey").render();
    grey2        = load(assets, "grey2").render();
    greybrown    = load(assets, "greybrown").render();
    invgrey      = load(assets, "invgrey").render();
    invgrey2     = load(assets, "invgrey2").render();
    invgreybrown = load(assets, "invgreybrown").render();
  }

  public Index get(int index) {
    switch (index) {
      case 1:  return brown;
      case 2:  return gold;
      case 3:  return grey;
      case 4:  return grey2;
      case 5:  return greybrown;
      case 6:  return invgrey;
      case 7:  return invgrey2;
      case 8:  return invgreybrown;
      default: return null;
    }
  }

  public Texture getTexture(int index) {
    return get(index).texture;
  }

  public String toString(int index) {
    return get(index).name;
  }

  private Index load(AssetManager assets, String fontName) {
    AssetDescriptor<Index> descriptor = getDescriptor(fontName);
    assets.load(descriptor);
    assets.finishLoadingAsset(descriptor);
    return assets.get(descriptor);
  }

  private static AssetDescriptor<Index> getDescriptor(String paletteName) {
    return new AssetDescriptor<>("data\\global\\items\\Palette\\" + paletteName + ".dat", Index.class);
  }

  @Override
  public void dispose() {
    brown.dispose();
    gold.dispose();
    grey.dispose();
    grey2.dispose();
    greybrown.dispose();
    invgrey.dispose();
    invgrey2.dispose();
    invgreybrown.dispose();
  }
}
