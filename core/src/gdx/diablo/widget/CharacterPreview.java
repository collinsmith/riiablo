package gdx.diablo.widget;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.Diablo;
import gdx.diablo.ItemCodes;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.excel.PlrMode;
import gdx.diablo.codec.excel.PlrType;
import gdx.diablo.codec.excel.WeaponClass;
import gdx.diablo.entity.Direction;

public class CharacterPreview extends Widget implements Disposable {

  private static final String CHARS = "data\\global\\chars\\";

  D2S d2s;
  AssetDescriptor[] assets;
  Animation anim;

  public CharacterPreview() {}

  @Override
  public void dispose() {
    if (assets != null) {
      for (int i = 0; i < COF.Component.NUM_COMPONENTS; i++) {
        if (assets[i] != null) {
          Diablo.assets.unload(assets[i].fileName);
          assets[i] = null;
        }
      }
    }
  }

  public void set(D2S d2s) {
    if (this.d2s == d2s) {
      return;
    }

    this.d2s = d2s;
    PlrType.Entry plrType = Diablo.files.PlrType.get(d2s.charClass);
    PlrMode.Entry plrMode = Diablo.files.PlrMode.get("TN");
    WeaponClass.Entry weaponClass = Diablo.files.WeaponClass.get("1hs");
    String cofId = plrType.Token + plrMode.Token + weaponClass.Code;
    COF cof = Diablo.cofs.chars_cof.lookup(cofId);

    String[] armorClasses = new String[16];
    for (int i = 0; i < 16; i++) {
      armorClasses[i] = ItemCodes.getCode(d2s.composites[i]);
    }
    //System.out.println(Arrays.toString(armorClasses));

    anim = Animation.newAnimation(cof);
    anim.setDirection(Direction.DOWN);
    if (assets == null) assets = new AssetDescriptor[COF.Component.NUM_COMPONENTS];
    else dispose();
    for (int i = 0; i < cof.getNumLayers(); i++) {
      COF.Layer layer = cof.getLayer(i);
      String component  = Diablo.files.Composit.get(layer.component).Token;
      String armorClass = armorClasses[layer.component];
      if (armorClass == null) armorClass = ItemCodes.getCode(ItemCodes.LIT);

      String wpnClass = layer.weaponClass;
      String path = CHARS + plrType.Token + "\\" + component + "\\" + plrType.Token + component + armorClass + plrMode.Token + wpnClass + ".dcc";

      assets[i] = new AssetDescriptor<>(path, DCC.class);
      Diablo.assets.load(assets[i]);
      Diablo.assets.finishLoadingAsset(assets[i]);
      DCC dcc = Diablo.assets.get(assets[i].fileName, DCC.class);
      anim.setLayer(layer, dcc, false).setTransform(d2s.colors[layer.component]);
    }

    anim.updateBox();
  }

  @Override
  public void draw(Batch batch, float a) {
    //if ((d2s.colors[1] & 0xFF) != 0xFF) {
    //  Texture colormap = Diablo.colormaps.get(d2s.colors[1] >>> 4);
    //  int colormapId = d2s.colors[1] & 0xF;
    //  ((PaletteIndexedBatch) batch).setColormap(colormap, colormapId);
    //}

    anim.act();
    anim.draw(batch, getX() + getWidth() / 2, getY() + 8);

    //if ((d2s.colors[1] & 0xFF) != 0xFF) {
    //  ((PaletteIndexedBatch) batch).resetColormap();
    //}
  }
}
