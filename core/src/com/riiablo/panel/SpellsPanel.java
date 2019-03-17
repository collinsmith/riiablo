package com.riiablo.panel;

import com.badlogic.gdx.assets.AssetDescriptor;
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
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.LabelButton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
    skillsRemaining.setPosition(256, 348); // 276, 359 middle
    addActor(skillsRemaining);

    float[] X = { 0, 15, 84, 153 };
    float[] Y = { 0, 370, 302, 234, 166, 98, 30 };
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      final int sLvl = gameScreen.player.skills.getLevel(i);
      final Skills.Entry skill = Riiablo.files.skills.get(i);
      final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      final Table details = new Table() {{
        final float SPACING = 2;
        final BitmapFont font = Riiablo.fonts.font16;
        setBackground(PaletteIndexedColorDrawable.MODAL_FONT16);
        add(new Label(Riiablo.string.lookup(desc.str_name), font, Riiablo.colors.green)).center().space(SPACING).row();
        add(new Label(font) {{
          // TODO: It might possible to optimize this more -- goal is to reverse lines since they are backwards for some reason
          String text = Riiablo.string.lookup(desc.str_long);
          String[] lines = StringUtils.split(text, '\n');
          ArrayUtils.reverse(lines);
          text = StringUtils.join(lines, '\n');
          setText(text);
          setAlignment(Align.center);
        }}).center().space(SPACING).row();
        add(new Label(Riiablo.string.lookup("skilldesc3") + skill.reqlevel, font)).center().space(SPACING).row();
        add().height(font.getLineHeight()).center().space(SPACING).row();
        for (int i = 0; i < desc.dsc2line.length; i++) {
          if (desc.dsc2line[i] <= 0) break;
          String str = calc(desc.dsc2line[i], desc, i, skill, sLvl);
          if (str != null) add(new Label(str, Riiablo.fonts.font16)).center().space(SPACING).row();
        }
        add().height(font.getLineHeight()).center().space(SPACING).row();
        add(new Label(Riiablo.string.lookup("StrSkill2") + sLvl, font)).center().space(SPACING).row();
        pack();
      }};
      Button button = new Button(new Button.ButtonStyle(
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel)),
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel + 1))) {{
            disabled = up;
      }}) {
        @Override
        public void draw(PaletteIndexedBatch batch, float parentAlpha) {
          super.draw(batch, parentAlpha);
          if (isOver()) {
            gameScreen.setDetails(details, null, SpellsPanel.this, this);
          }
        }
      };
      button.setPosition(X[desc.SkillColumn], Y[desc.SkillRow]);
      //button.setSize(48, 48);

      // TODO: can be lazily init if default is 0
      Label skillLevel = new Label(sLvl > 0 ? Integer.toString(sLvl) : "", sLvl > 9 ? Riiablo.fonts.fontformal10 : Riiablo.fonts.font16);
      skillLevel.setAlignment(Align.center);
      //skillLevel.setSize(16, 14);
      //skillLevel.setPosition(X[desc.SkillColumn] + 44, Y[desc.SkillRow] - 12);
      skillLevel.setPosition(X[desc.SkillColumn] + 52, Y[desc.SkillRow] - 5, Align.center);
      button.setUserObject(skillLevel);
      button.setDisabled(sLvl <= 0);

      Tab tab = tabs[desc.SkillPage];
      tab.addActor(button);
      tab.addActor(skillLevel);
    }

    tabs[1].setVisible(true);
    //setDebug(true, true);
  }

  private String calc(int func, SkillDesc.Entry desc, int i, Skills.Entry skill, int lvl) {
    switch(func) {
      case 1:  return String.format("%s%s", Riiablo.string.lookup(desc.str_mana), getManaCost(skill, lvl)); // fire bolt
      case 3:  return String.format("%s%s%s", Riiablo.string.lookup(desc.dsc2texta[i]), eval(skill, lvl, desc.dsc2calca[i]), Riiablo.string.lookup(desc.dsc2textb[i])); // static field
      case 5:  return String.format("%s%s", Riiablo.string.lookup(desc.dsc2texta[i]), eval(skill, lvl, desc.dsc2calca[i])); // inferno
      case 19: return String.format("%s%s%s%s", (desc.dsc2textb[i].length() > 0 ? Riiablo.string.lookup(desc.dsc2textb[i]) : ""), Riiablo.string.lookup(desc.dsc2texta[i]), eval(skill, lvl, desc.dsc2calca[i]) * 2f/3f, Riiablo.string.lookup("StrSkill26")); // glacial spike
      case 28: return String.format("%s1%s", Riiablo.string.lookup("StrSkill18"), Riiablo.string.lookup("StrSkill36")); // fire ball
      default: return null;
    }
  }

  private float eval(Skills.Entry skill, int lvl, String calc) {
    if (calc.startsWith("par")) {
      return skill.Param[calc.charAt(3) - '1'];
    } else if (calc.startsWith("ln")) {
      int a = skill.Param[calc.charAt(2) - '1'];
      int b = skill.Param[calc.charAt(3) - '1'];
      return a + lvl * b;
    } else if (calc.startsWith("dm")) {
      int a = skill.Param[calc.charAt(2) - '1'];
      int b = skill.Param[calc.charAt(3) - '1'];
      return ((110*lvl) * (b-a))/(100 * (lvl+6)) + a;
    } else {
      return -1;
    }
  }

  private float getManaCost(Skills.Entry skill, int lvl) {
    return (1 << skill.manashift) / 256f * skill.mana + skill.lvlmana * lvl;
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
