package com.riiablo.widget;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.D2S;
import com.riiablo.codec.DC6;
import com.riiablo.entity.CharacterPreview;
import com.riiablo.loader.DC6Loader;

public class CharacterSelectButton extends Table implements Disposable {
  public static final int WIDTH  = 272;
  public static final int HEIGHT = 93;

  static final AssetDescriptor<DC6> charselectboxDescriptor = new AssetDescriptor<>("data\\global\\ui\\CharSelect\\charselectbox.dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  static TextureRegion charselectbox;

  public static void loadBox() {
    Riiablo.assets.load(charselectboxDescriptor);
  }

  public static void unloadBox() {
    Riiablo.assets.unload(charselectboxDescriptor.fileName);
    charselectbox = null;
  }

  D2S d2s;
  EntityWrapper preview;
  Label title;
  Label name;
  Label levelClass;
  Label expansion;

  public CharacterSelectButton(D2S d2s) {
    setSize(WIDTH, HEIGHT);
    setTouchable(Touchable.enabled);

    preview = new EntityWrapper();

    title = new Label(Riiablo.fonts.font16);
    title.setColor(Riiablo.colors.gold);

    name = new Label(Riiablo.fonts.font16);
    name.setColor(Riiablo.colors.gold);

    levelClass = new Label(Riiablo.fonts.font16);

    expansion = new Label(Riiablo.fonts.font16);
    expansion.setColor(Riiablo.colors.green);

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
    //preview.dispose(); // TODO
  }

  public void set(D2S d2s) {
    this.d2s = d2s;
    setName(d2s.name);
    preview.set(new CharacterPreview(d2s));
    title.setText(d2s.getProgressionString()); // TODO: i18n? This may be hard-coded in-game
    name.setText(d2s.name);
    name.setColor(d2s.isHardcore() ? Riiablo.colors.red : Riiablo.colors.gold);
    levelClass.setText(Riiablo.string.format(5017, d2s.level) + " " + Riiablo.string.lookup(CharacterClass.get(d2s.charClass).name));
    expansion.setText(d2s.isExpansion() ? 11077 : -1);
  }

  public D2S getD2S() {
    return d2s;
  }

  public void select() {
    if (charselectbox == null || charselectbox.getTexture().getTextureObjectHandle() == 0) {
      Riiablo.assets.finishLoadingAsset(charselectboxDescriptor);
      charselectbox = Riiablo.assets.get(charselectboxDescriptor).getTexture();
    }

    setBackground(new TextureRegionDrawable(charselectbox));
  }

  public void deselect() {
    setBackground((Drawable) null);
  }
}
