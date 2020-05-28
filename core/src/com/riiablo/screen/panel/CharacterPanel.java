package com.riiablo.screen.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.Cvars;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.item.Attributes;
import com.riiablo.item.Stat;
import com.riiablo.loader.DC6Loader;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.StatLabel;

import java.text.NumberFormat;

public class CharacterPanel extends WidgetGroup implements Disposable {

  final AssetDescriptor<DC6> invcharDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invchar;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  public CharacterPanel() {
    Riiablo.assets.load(invcharDescriptor);
    Riiablo.assets.finishLoadingAsset(invcharDescriptor);
    invchar = Riiablo.assets.get(invcharDescriptor).getTexture(0);
    setSize(invchar.getRegionWidth(), invchar.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    btnExit = new Button(new Button.ButtonStyle() {{
      Riiablo.assets.load(buysellbtnDescriptor);
      Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Riiablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(128, 11);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    Label name = new Label(Riiablo.charData.name, Riiablo.fonts.font16);
    name.setPosition(12, getHeight() - 24);
    name.setSize(168, 13);
    name.setAlignment(Align.center);
    addActor(name);

    Table level = new Table();
    level.setPosition(12, getHeight() - 65);
    level.setSize(42, 33);
    level.add(new Label(4057, Riiablo.fonts.ReallyTheLastSucker)).row();
    level.add(new Label(Integer.toString(Riiablo.charData.getStats().get(Stat.level).value()), Riiablo.fonts.font16)).growY().row();
    addActor(level);

    Table exp = new Table();
    exp.setPosition(66, getHeight() - 65);
    exp.setSize(114, 33);
    exp.add(new Label(4058, Riiablo.fonts.ReallyTheLastSucker)).row();
    exp.add(new Label(NumberFormat.getInstance(Cvars.Client.Locale.get()).format(Riiablo.charData.getStats().get(Stat.experience).toLong()), Riiablo.fonts.font16)).growY().row();
    addActor(exp);

    Label clazz = new Label(Riiablo.charData.classId.name, Riiablo.fonts.font16);
    clazz.setPosition(194, getHeight() - 24);
    clazz.setSize(114, 13);
    clazz.setAlignment(Align.center);
    addActor(clazz);

    Table nextLevel = new Table();
    nextLevel.setPosition(194, getHeight() - 65);
    nextLevel.setSize(114, 33);
    nextLevel.add(new Label(4059, Riiablo.fonts.ReallyTheLastSucker)).row();
    nextLevel.add(new Label("...", Riiablo.fonts.font16)).growY().row();
    addActor(nextLevel);

    Label strLabel = new Label(4060, Riiablo.fonts.ReallyTheLastSucker);
    strLabel.setPosition(11, getHeight() - 100);
    strLabel.setSize(63, 16);
    strLabel.setAlignment(Align.center);
    addActor(strLabel);

    Label str = createStatLabel(Stat.strength);
    str.setPosition(78, getHeight() - 100);
    str.setSize(36, 16);
    addActor(str);

    Label dexLabel = new Label(4062, Riiablo.fonts.ReallyTheLastSucker);
    dexLabel.setPosition(11, getHeight() - 162);
    dexLabel.setSize(63, 16);
    dexLabel.setAlignment(Align.center);
    addActor(dexLabel);

    Label dex = createStatLabel(Stat.dexterity);
    dex.setPosition(78, getHeight() - 162);
    dex.setSize(36, 16);
    addActor(dex);

    Label defenseLabel = Label.i18n("strchrdef", Riiablo.fonts.ReallyTheLastSucker);
    defenseLabel.setPosition(165, getHeight() - 210);
    defenseLabel.setSize(108, 16);
    defenseLabel.setAlignment(Align.center);
    addActor(defenseLabel);

    Label armorclass = createStatLabel(Stat.armorclass);
    armorclass.setPosition(272, getHeight() - 210);
    armorclass.setSize(40, 16);
    addActor(armorclass);

    Label vitLabel = new Label(4066, Riiablo.fonts.ReallyTheLastSucker);
    vitLabel.setPosition(11, getHeight() - 248);
    vitLabel.setSize(63, 16);
    vitLabel.setAlignment(Align.center);
    addActor(vitLabel);

    Label vit = createStatLabel(Stat.vitality);
    vit.setPosition(78, getHeight() - 248);
    vit.setSize(36, 16);
    addActor(vit);

    Label staminaLabel = Label.i18n("strchrstm", Riiablo.fonts.ReallyTheLastSucker);
    staminaLabel.setPosition(165, getHeight() - 248);
    staminaLabel.setSize(63, 16);
    staminaLabel.setAlignment(Align.center);
    addActor(staminaLabel);

    Label stamina = createStatLabel(Stat.stamina);
    stamina.setPosition(235, getHeight() - 248);
    stamina.setSize(36, 16);
    addActor(stamina);

    Label maxstamina = createStatLabel(Stat.maxstamina);
    maxstamina.setPosition(275, getHeight() - 248);
    maxstamina.setSize(36, 16);
    addActor(maxstamina);

    Label lifeLabel = Label.i18n("strchrlif", Riiablo.fonts.ReallyTheLastSucker);
    lifeLabel.setPosition(165, getHeight() - 272);
    lifeLabel.setSize(63, 16);
    lifeLabel.setAlignment(Align.center);
    addActor(lifeLabel);

    Label hitpoints = createStatLabel(Stat.hitpoints);
    hitpoints.setPosition(235, getHeight() - 272);
    hitpoints.setSize(36, 16);
    addActor(hitpoints);

    Label maxhp = createStatLabel(Stat.maxhp);
    maxhp.setPosition(275, getHeight() - 272);
    maxhp.setSize(36, 16);
    addActor(maxhp);

    Label eneLabel = new Label(4069, Riiablo.fonts.ReallyTheLastSucker);
    eneLabel.setPosition(11, getHeight() - 310);
    eneLabel.setSize(63, 16);
    eneLabel.setAlignment(Align.center);
    addActor(eneLabel);

    Label ene = createStatLabel(Stat.energy);
    ene.setPosition(78, getHeight() - 310);
    ene.setSize(36, 16);
    addActor(ene);

    Label manaLabel = Label.i18n("strchrman", Riiablo.fonts.ReallyTheLastSucker);
    manaLabel.setPosition(165, getHeight() - 310);
    manaLabel.setSize(63, 16);
    manaLabel.setAlignment(Align.center);
    addActor(manaLabel);

    Label mana = createStatLabel(Stat.mana);
    mana.setPosition(235, getHeight() - 310);
    mana.setSize(36, 16);
    addActor(mana);

    Label maxmana = createStatLabel(Stat.maxmana);
    maxmana.setPosition(275, getHeight() - 310);
    maxmana.setSize(36, 16);
    addActor(maxmana);

    Label fireResLabel = new Label(4071, Riiablo.fonts.ReallyTheLastSucker);
    fireResLabel.setPosition(175, getHeight() - 349);
    fireResLabel.setSize(94, 16);
    fireResLabel.setAlignment(Align.center);
    addActor(fireResLabel);

    Label fireRes = createStatLabel(Stat.fireresist, StatLabel.Colorizer.RESISTANCE);
    fireRes.setPosition(273, getHeight() - 349);
    fireRes.setSize(36, 16);
    addActor(fireRes);

    Label coldResLabel = new Label(4072, Riiablo.fonts.ReallyTheLastSucker);
    coldResLabel.setPosition(175, getHeight() - 373);
    coldResLabel.setSize(94, 16);
    coldResLabel.setAlignment(Align.center);
    addActor(coldResLabel);

    Label coldRes = createStatLabel(Stat.coldresist, StatLabel.Colorizer.RESISTANCE);
    coldRes.setPosition(273, getHeight() - 373);
    coldRes.setSize(36, 16);
    addActor(coldRes);

    Label lightningResLabel = new Label(4073, Riiablo.fonts.ReallyTheLastSucker);
    lightningResLabel.setPosition(175, getHeight() - 397);
    lightningResLabel.setSize(94, 16);
    lightningResLabel.setAlignment(Align.center);
    addActor(lightningResLabel);

    Label lightningRes = createStatLabel(Stat.lightresist, StatLabel.Colorizer.RESISTANCE);
    lightningRes.setPosition(273, getHeight() - 397);
    lightningRes.setSize(36, 16);
    addActor(lightningRes);

    Label poisonResLabel = new Label(4074, Riiablo.fonts.ReallyTheLastSucker);
    poisonResLabel.setPosition(175, getHeight() - 421);
    poisonResLabel.setSize(94, 16);
    poisonResLabel.setAlignment(Align.center);
    addActor(poisonResLabel);

    Label poisonRes = createStatLabel(Stat.poisonresist, StatLabel.Colorizer.RESISTANCE);
    poisonRes.setPosition(273, getHeight() - 421);
    poisonRes.setSize(36, 16);
    addActor(poisonRes);

    //setDebug(true, true);
  }

  private Label createStatLabel(int statId) {
    return createStatLabel(statId, StatLabel.Colorizer.DEFAULT);
  }

  private Label createStatLabel(int statId, StatLabel.Colorizer colorizer) {
    Attributes attrs = Riiablo.charData.getStats();
    return new StatLabel(attrs, statId, colorizer);
  }

  private BitmapFont getFont(int length) {
    if (length > 6) {
      return Riiablo.fonts.ReallyTheLastSucker;
    } else if (length > 3) {
      return Riiablo.fonts.font8;
    } else {
      return Riiablo.fonts.font16;
    }
  }

  private Color getColor(boolean mod, Stat stat, int value) {
    if (value < 0) {
      return Riiablo.colors.red;
    }

    if (!stat.entry.maxstat.isEmpty()) {
      int id = Riiablo.files.ItemStatCost.index(stat.entry.maxstat);
      Stat maxstat = Riiablo.charData.getStats().get(id);
      if (maxstat != null && value >= maxstat.value()) {
        return Riiablo.colors.gold;
      }
    }

    if (mod && Riiablo.charData.getStats().get(stat.id).isModified()) {
      return Riiablo.colors.blue;
    }

    return Riiablo.colors.white;
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(invcharDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(invchar, getX(), getY());
    super.draw(batch, a);
  }
}
