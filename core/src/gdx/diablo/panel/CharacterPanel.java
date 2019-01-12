package gdx.diablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import java.text.NumberFormat;

import gdx.diablo.Cvars;
import gdx.diablo.Diablo;
import gdx.diablo.codec.DC6;
import gdx.diablo.entity.Player;
import gdx.diablo.loader.DC6Loader;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Button;
import gdx.diablo.widget.Label;

public class CharacterPanel extends WidgetGroup implements Disposable {

  final AssetDescriptor<DC6> invcharDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\invchar6.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
  TextureRegion invchar;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class);
  Button btnExit;

  final GameScreen gameScreen;

  public CharacterPanel(GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    Diablo.assets.load(invcharDescriptor);
    Diablo.assets.finishLoadingAsset(invcharDescriptor);
    invchar = Diablo.assets.get(invcharDescriptor).getTexture(0);
    setSize(invchar.getRegionWidth(), invchar.getRegionHeight());
    setVisible(false);

    btnExit = new Button(new Button.ButtonStyle() {{
      Diablo.assets.load(buysellbtnDescriptor);
      Diablo.assets.finishLoadingAsset(buysellbtnDescriptor);
      up   = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(10));
      down = new TextureRegionDrawable(Diablo.assets.get(buysellbtnDescriptor).getTexture(11));
    }});
    btnExit.setPosition(128, 11);
    btnExit.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        setVisible(false);
      }
    });
    addActor(btnExit);

    Player player = gameScreen.player;

    Label name = new Label(player.getName(), Diablo.fonts.font16);
    name.setPosition(12, getHeight() - 24);
    name.setSize(168, 13);
    name.setAlignment(Align.center);
    addActor(name);

    Table level = new Table();
    level.setPosition(12, getHeight() - 65);
    level.setSize(42, 33);
    level.add(new Label(4057, Diablo.fonts.ReallyTheLastSucker)).row();
    level.add(new Label(Integer.toString(player.getLevel()), Diablo.fonts.font16)).growY().row();
    addActor(level);

    Table exp = new Table();
    exp.setPosition(66, getHeight() - 65);
    exp.setSize(114, 33);
    exp.add(new Label(4058, Diablo.fonts.ReallyTheLastSucker)).row();
    exp.add(new Label(NumberFormat.getInstance(Cvars.Client.Locale.get()).format(player.getExperience()), Diablo.fonts.font16)).growY().row();
    addActor(exp);

    Label clazz = new Label(player.getCharClass().name, Diablo.fonts.font16);
    clazz.setPosition(194, getHeight() - 24);
    clazz.setSize(114, 13);
    clazz.setAlignment(Align.center);
    addActor(clazz);

    Table nextLevel = new Table();
    nextLevel.setPosition(194, getHeight() - 65);
    nextLevel.setSize(114, 33);
    nextLevel.add(new Label(4059, Diablo.fonts.ReallyTheLastSucker)).row();
    nextLevel.add(new Label("...", Diablo.fonts.font16)).growY().row();
    addActor(nextLevel);

    Label strLabel = new Label(4060, Diablo.fonts.ReallyTheLastSucker);
    strLabel.setPosition(11, getHeight() - 100);
    strLabel.setSize(63, 16);
    strLabel.setAlignment(Align.center);
    addActor(strLabel);

    Label str = new Label(Integer.toString(player.getStrength()), Diablo.fonts.font16);
    str.setPosition(78, getHeight() - 100);
    str.setSize(36, 16);
    str.setAlignment(Align.center);
    addActor(str);

    Label dexLabel = new Label(4062, Diablo.fonts.ReallyTheLastSucker);
    dexLabel.setPosition(11, getHeight() - 162);
    dexLabel.setSize(63, 16);
    dexLabel.setAlignment(Align.center);
    addActor(dexLabel);

    Label dex = new Label(Integer.toString(player.getDexterity()), Diablo.fonts.font16);
    dex.setPosition(78, getHeight() - 162);
    dex.setSize(36, 16);
    dex.setAlignment(Align.center);
    addActor(dex);

    Label vitLabel = new Label(4066, Diablo.fonts.ReallyTheLastSucker);
    vitLabel.setPosition(11, getHeight() - 248);
    vitLabel.setSize(63, 16);
    vitLabel.setAlignment(Align.center);
    addActor(vitLabel);

    Label vit = new Label(Integer.toString(player.getVitality()), Diablo.fonts.font16);
    vit.setPosition(78, getHeight() - 248);
    vit.setSize(36, 16);
    vit.setAlignment(Align.center);
    addActor(vit);

    Label eneLabel = new Label(4069, Diablo.fonts.ReallyTheLastSucker);
    eneLabel.setPosition(11, getHeight() - 310);
    eneLabel.setSize(63, 16);
    eneLabel.setAlignment(Align.center);
    addActor(eneLabel);

    Label ene = new Label(Integer.toString(player.getEnergy()), Diablo.fonts.font16);
    ene.setPosition(78, getHeight() - 310);
    ene.setSize(36, 16);
    ene.setAlignment(Align.center);
    addActor(ene);

    Label fireResLabel = new Label(4071, Diablo.fonts.ReallyTheLastSucker);
    fireResLabel.setPosition(175, getHeight() - 349);
    fireResLabel.setSize(94, 16);
    fireResLabel.setAlignment(Align.center);
    addActor(fireResLabel);

    Label fireRes = new Label(Integer.toString(player.getFireResistance()), Diablo.fonts.font16);
    fireRes.setPosition(273, getHeight() - 349);
    fireRes.setSize(36, 16);
    fireRes.setAlignment(Align.center);
    addActor(fireRes);

    Label coldResLabel = new Label(4072, Diablo.fonts.ReallyTheLastSucker);
    coldResLabel.setPosition(175, getHeight() - 373);
    coldResLabel.setSize(94, 16);
    coldResLabel.setAlignment(Align.center);
    addActor(coldResLabel);

    Label coldRes = new Label(Integer.toString(player.getColdResistance()), Diablo.fonts.font16);
    coldRes.setPosition(273, getHeight() - 373);
    coldRes.setSize(36, 16);
    coldRes.setAlignment(Align.center);
    addActor(coldRes);

    Label lightningResLabel = new Label(4073, Diablo.fonts.ReallyTheLastSucker);
    lightningResLabel.setPosition(175, getHeight() - 397);
    lightningResLabel.setSize(94, 16);
    lightningResLabel.setAlignment(Align.center);
    addActor(lightningResLabel);

    Label lightningRes = new Label(Integer.toString(player.getLightningResistance()), Diablo.fonts.font16);
    lightningRes.setPosition(273, getHeight() - 397);
    lightningRes.setSize(36, 16);
    lightningRes.setAlignment(Align.center);
    addActor(lightningRes);

    Label poisonResLabel = new Label(4074, Diablo.fonts.ReallyTheLastSucker);
    poisonResLabel.setPosition(175, getHeight() - 421);
    poisonResLabel.setSize(94, 16);
    poisonResLabel.setAlignment(Align.center);
    addActor(poisonResLabel);

    Label poisonRes = new Label(Integer.toString(player.getPoisonResistance()), Diablo.fonts.font16);
    poisonRes.setPosition(273, getHeight() - 421);
    poisonRes.setSize(36, 16);
    poisonRes.setAlignment(Align.center);
    addActor(poisonRes);

    setDebug(true, true);
  }

  @Override
  public void dispose() {
    Diablo.assets.unload(invcharDescriptor.fileName);
    Diablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  @Override
  public void draw(Batch batch, float a) {
    batch.draw(invchar, getX(), getY());
    super.draw(batch, a);
  }
}
