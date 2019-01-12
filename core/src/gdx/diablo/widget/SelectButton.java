package gdx.diablo.widget;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import gdx.diablo.CharClass;
import gdx.diablo.Diablo;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.DC6;
import gdx.diablo.loader.DC6Loader;

public class SelectButton extends Table implements Disposable {
  public static final int WIDTH  = 272;
  public static final int HEIGHT = 93;

  static final AssetDescriptor<DC6> charselectboxDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\charselectbox.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  static TextureRegion charselectbox;

  public static void load() {
    Diablo.assets.load(charselectboxDescriptor);
  }

  public static void unload() {
    Diablo.assets.unload(charselectboxDescriptor.fileName);
  }

  D2S d2s;
  CharacterPreview preview;
  Label title;
  Label name;
  Label levelClass;
  Label expansion;

  public SelectButton(D2S d2s) {
    setSize(WIDTH, HEIGHT);
    setTouchable(Touchable.enabled);

    preview = new CharacterPreview();

    title      = new Label(Diablo.fonts.font16);
    title.setColor(Diablo.colors.unique);

    name       = new Label(Diablo.fonts.font16);
    name.setColor(Diablo.colors.unique);

    levelClass = new Label(Diablo.fonts.font16);

    expansion  = new Label(Diablo.fonts.font16);
    expansion.setColor(Diablo.colors.set);

    Table previewColumn = new Table();
    previewColumn.add(preview).width(72).growY();

    Table textColumn = new Table();
    textColumn.add(title).left().row();
    textColumn.add(name).left().row();
    textColumn.add(levelClass).left().row();
    textColumn.add(expansion).left().row();

    align(Align.topLeft);
    pad(4);
    add(previewColumn).growY();
    add(textColumn).growX().top();

    //setDebug(true, true);
    set(d2s);
  }

  @Override
  public void dispose() {
    preview.dispose();
  }

  public void set(D2S d2s) {
    this.d2s = d2s;
    setName(d2s.name);
    preview.set(d2s);
    title.setText(d2s.getProgressionString());
    name.setText(d2s.name);
    name.setColor(d2s.isHardcore() ? Diablo.colors.server : Diablo.colors.unique);
    levelClass.setText(Diablo.string.format(5017, d2s.level) + " " + Diablo.string.lookup(CharClass.get(d2s.charClass).name));
    expansion.setText(d2s.isExpansion() ? 11077 : -1);
  }

  public D2S getD2S() {
    return d2s;
  }

  public void select() {
    if (charselectbox == null) {
      Diablo.assets.finishLoadingAsset(charselectboxDescriptor);
      charselectbox = Diablo.assets.get(charselectboxDescriptor).getTexture();
    }

    setBackground(new TextureRegionDrawable(charselectbox));
  }

  public void deselect() {
    setBackground((Drawable) null);
  }
}
