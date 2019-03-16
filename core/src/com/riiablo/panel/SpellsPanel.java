package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.LabelButton;

public class SpellsPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "SpellsPanel";
  private static final String SPELLS_PATH = "data\\global\\ui\\SPELLS\\";

  final AssetDescriptor<DC6> skltreeDescriptor;
  TextureRegion skltree;

  final AssetDescriptor<DC6> SkilliconDescriptor;
  DC6 Skillicon;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);

  final GameScreen gameScreen;

  public SpellsPanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;

    CharacterClass charClass = gameScreen.player.charClass;
    skltreeDescriptor = new AssetDescriptor<>(SPELLS_PATH + charClass.spellsBackground + ".dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
    Riiablo.assets.load(skltreeDescriptor);
    Riiablo.assets.finishLoadingAsset(skltreeDescriptor);
    skltree = Riiablo.assets.get(skltreeDescriptor).getTexture(0);
    setSize(skltree.getRegionWidth(), skltree.getRegionHeight());
    setTouchable(Touchable.enabled);
    setVisible(false);

    SkilliconDescriptor = new AssetDescriptor<>(SPELLS_PATH + charClass.spellIcons + ".dc6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
    Riiablo.assets.load(SkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SkilliconDescriptor);
    Skillicon = Riiablo.assets.get(SkilliconDescriptor);

    Riiablo.assets.load(buysellbtnDescriptor);
    Riiablo.assets.finishLoadingAsset(buysellbtnDescriptor);
    DC buysellbtn = Riiablo.assets.get(buysellbtnDescriptor);
    TextureRegionDrawable exitUp   = new TextureRegionDrawable(buysellbtn.getTexture(10));
    TextureRegionDrawable exitDown = new TextureRegionDrawable(buysellbtn.getTexture(11));
    Button.ButtonStyle exitButtonStyle = new Button.ButtonStyle(exitUp, exitDown);

    ClickListener closeListener = new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        SpellsPanel.this.setVisible(false);
      }
    };
    final float[] exitY = {31, 115, 187};
    final Tab[] tabs = new Tab[4];
    for (int i = 1; i < tabs.length; i++) {
      Tab tab = tabs[i] = new Tab(Riiablo.assets.get(skltreeDescriptor).getTexture(i));
      tab.setSize(getWidth(), getHeight());
      tab.setPosition(0, 0);
      tab.setVisible(false);
      addActor(tab);

      Button exit = new Button(exitButtonStyle);
      exit.addListener(closeListener);
      exit.setPosition(exitY[charClass.spellCloseCol[i - 1]], 31, Align.center);
      tab.addActor(exit);
    }

    StringBuilder builder = new StringBuilder(32);
    float x = getWidth() - 90, y = 0;
    LabelButton[] actors = new LabelButton[3];
    for (int i = 0; i < actors.length; i++) {
      String[] spellTree = charClass.spellTree[i];
      builder.setLength(0);
      for (int j = 0; j < spellTree.length; j++) {
        builder
            .append(Riiablo.string.lookup(spellTree[j]))
            .append('\n');
      }
      builder.setLength(builder.length() - 1);
      final LabelButton actor = actors[i] = new LabelButton(builder.toString(), Riiablo.fonts.font16);
      actor.setAlignment(Align.center);
      actor.setPosition(x, y);
      actor.setSize(90, getHeight() / 4);
      actor.setUserObject(tabs[i + 1]);
      actor.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          for (Tab tab : tabs) if (tab != null) tab.setVisible(false);
          Tab tab = (Tab) actor.getUserObject();
          tab.setVisible(true);
        }
      });
      addActor(actor);
      y += actor.getHeight();
    }

    builder.setLength(0);
    builder
        .append(Riiablo.string.lookup("StrSklTree1"))
        .append('\n')
        .append(Riiablo.string.lookup("StrSklTree2"))
        .append('\n')
        .append(Riiablo.string.lookup("StrSklTree3"));
    Label header = new Label(builder.toString(), Riiablo.fonts.font16);
    header.setAlignment(Align.center);
    header.setSize(90, getHeight() / 8);
    header.setPosition(x, y + header.getHeight());
    addActor(header);

    Label skillsRemaining = new Label("0", Riiablo.fonts.font16);
    skillsRemaining.setAlignment(Align.center);
    skillsRemaining.setSize(40, 21);
    skillsRemaining.setPosition(256, 348);
    addActor(skillsRemaining);

    float[] X = { 0, 15, 84, 153 };
    float[] Y = { 0, 370, 302, 234, 166, 98, 30 };
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      Skills.Entry skill = Riiablo.files.skills.get(i);
      SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      Button button = new Button(new Button.ButtonStyle(
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel)),
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel + 1))));
      button.setPosition(X[desc.SkillColumn], Y[desc.SkillRow]);
      //button.setSize(48, 48);

      Tab tab = tabs[desc.SkillPage];
      tab.addActor(button);
    }

    tabs[1].setVisible(true);
    //setDebug(true, true);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.draw(skltree, getX(), getY());
    super.draw(batch, parentAlpha);
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(skltreeDescriptor.fileName);
    Riiablo.assets.unload(SkilliconDescriptor.fileName);
    Riiablo.assets.unload(buysellbtnDescriptor.fileName);
  }

  private static class Tab extends WidgetGroup {
    TextureRegion background;

    public Tab(TextureRegion background) {
      this.background = background;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      batch.draw(background, getX(), getY());
      super.draw(batch, parentAlpha);
    }
  }
}
