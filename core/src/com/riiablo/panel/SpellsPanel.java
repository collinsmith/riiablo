package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;

public class SpellsPanel extends WidgetGroup implements Disposable {
  private static final String TAG = "SpellsPanel";
  private static final String SPELLS_PATH = "data\\global\\ui\\SPELLS\\";

  final AssetDescriptor<DC6> skltreeDescriptor;
  TextureRegion skltree;
  TextureRegion skltreeTabs[];

  final AssetDescriptor<DC6> SkilliconDescriptor;
  DC6 Skillicon;

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

    final Tab[] tabs = new Tab[4];
    for (int i = 1; i < tabs.length; i++) {
      Tab tab = tabs[i] = new Tab(Riiablo.assets.get(skltreeDescriptor).getTexture(i));
      tab.setSize(getWidth(), getHeight());
      tab.setPosition(0, 0);
      tab.setVisible(false);
      addActor(tab);
    }

    float x = getWidth() - 90, y = 0;
    Actor[] actors = new Actor[3];
    for (int i = 0; i < actors.length; i++) {
      final Actor actor = actors[i] = new Actor();
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

    float[] X = { 0, 15, 84, 153 };
    float[] Y = { 0, 370, 302, 234, 166, 98, 30 };
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      Skills.Entry skill = Riiablo.files.skills.get(i);
      SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      Button button = new Button(new Button.ButtonStyle(
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel)),
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel + 1))));
      button.setPosition(X[desc.SkillColumn], Y[desc.SkillRow]);
      button.setSize(48, 48);

      Tab tab = tabs[desc.SkillPage];
      tab.addActor(button);
    }

    setDebug(true, true);
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
