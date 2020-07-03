package com.riiablo.screen.panel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntIntMap;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.graphics.PaletteIndexedColorDrawable;
import com.riiablo.item.Stat;
import com.riiablo.loader.DC6Loader;
import com.riiablo.save.CharData;
import com.riiablo.widget.Button;
import com.riiablo.widget.Label;
import com.riiablo.widget.LabelButton;

public class SpellsPanel extends WidgetGroup implements Disposable, CharData.SkillListener {
  private static final String TAG = "SpellsPanel";
  private static final String SPELLS_PATH = "data\\global\\ui\\SPELLS\\";

  final AssetDescriptor<DC6> skltreeDescriptor;
  TextureRegion skltree;

  final AssetDescriptor<DC6> SkilliconDescriptor;
  DC6 Skillicon;

  final AssetDescriptor<DC6> buysellbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\buysellbtn.DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);

  final SkillButton[] buttons;

  static final com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle SMALL_LABEL_STYLE
      = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(Riiablo.fonts.fontformal10, null);
  static final com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle LARGE_LABEL_STYLE
      = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(Riiablo.fonts.font16, null);

  public SpellsPanel() {
    CharacterClass charClass = Riiablo.charData.classId;
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
    buttons = new SkillButton[charClass.lastSpell - charClass.firstSpell];
    for (int i = charClass.firstSpell; i < charClass.lastSpell; i++) {
      final Skills.Entry skill = Riiablo.files.skills.get(i);
      final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
      SkillButton button = buttons[i - charClass.firstSpell] = new SkillButton(skill, desc);
      button.setPosition(X[desc.SkillColumn], Y[desc.SkillRow]);
      tabs[desc.SkillPage].addActor(button);
    }

    tabs[1].setVisible(true);
    //setDebug(true, true);

    Riiablo.charData.addSkillListener(this);
  }

  @Override
  public void onChanged(CharData client, IntIntMap skills, Array<Stat> chargedSkills) {
    for (int i = client.classId.firstSpell; i < client.classId.lastSpell; i++) {
      SkillButton button = buttons[i - client.classId.firstSpell];
      button.update(skills.get(i, 0));
    }
  }

  private static Color getColor(String str) {
    int i = NumberUtils.toInt(str, 0);
    switch (i) {
      case 0:  return Riiablo.colors.white;
      case 1:  return Riiablo.colors.red;
      case 2:  return Riiablo.colors.green;
      case 3:  return Riiablo.colors.blue;
      case 4:  return Riiablo.colors.gold;
      case 5:  return Riiablo.colors.grey;
      case 6:  return Color.CLEAR;
      case 7:  return Riiablo.colors.c7;
      case 8:  return Riiablo.colors.orange;
      case 9:  return Riiablo.colors.yellow;
      default: return Riiablo.colors.white;
    }
  }

  private String calc(SkillDesc.Entry desc, int i, int[] descline, String[] desctexta, String[] desctextb, String[] desccalca, String[] desccalcb, Skills.Entry skill, int lvl) {
    switch(descline[i]) {
      case 1:  return String.format("%s%s", Riiablo.string.lookup(desc.str_mana), getManaCost(skill, lvl)); // Mana Cost: 3
      case 2:  return String.format("%s+%s%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctextb[i])); // Fire Damage: +30 percent
      case 3:  return String.format("%s%s%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctextb[i])); // Weakens enemies by 25 percent
      case 4:  return String.format("%s+%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i])); // Heals +2
      case 5:  return String.format("%s%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i])); // Minimum mana to cast: 4
      case 6:  return String.format("+%s%s", eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctexta[i])); // +30 percent
      case 7:  return String.format("%s%s", eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctexta[i])); // 13 percent chance
      case 8:  return String.format("%s%s", Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i])); // 200 Attack Rating
      case 9:  return String.format("%s%s%s+%s", Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i]), Riiablo.string.lookup("StrSkill4"), eval(skill, lvl, desccalca[i])); // Damage bonus
      //case 10: return String.format("%s%s", Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i])); // (Elem) Damage: X-Y
      //case 11: return String.format("%s%s", Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i])); // Same as above?
      case 12: return String.format("%s%s%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]), Riiablo.string.lookup("StrSkill16")); // Duration: 4 seconds
      case 13: return String.format("%s%s", Riiablo.string.lookup("StrSkill42"), eval(skill, lvl, desccalca[i])); // Life: 100
      //case 14: return String.format()
      //case 15: return String.format("%s:%s", Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i]));
      case 19: return String.format("%s%s%s%s", (desctextb[i].length() > 0 ? Riiablo.string.lookup(desctextb[i]) : ""), Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]) * 2f/3f, Riiablo.string.lookup("StrSkill26")); // Radius: 2.6 yards
      case 28: return String.format("%s1%s", Riiablo.string.lookup("StrSkill18"), Riiablo.string.lookup("StrSkill36")); // Radius 1 yard
      case 40: return String.format(Riiablo.string.lookup(desctexta[i]), Riiablo.string.lookup(desctextb[i]));
      case 63: return String.format("%s: +%s%% %s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctextb[i]));
      case 67: return String.format("%s: +%s%s", Riiablo.string.lookup(desctexta[i]), eval(skill, lvl, desccalca[i]), Riiablo.string.lookup(desctextb[i]));
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

  private class SkillButton extends Button {
    final Skills.Entry skill;
    final SkillDesc.Entry desc;
    final Details details;
    final Label label;

    int sLvl;

    SkillButton(Skills.Entry skill, SkillDesc.Entry desc) {
      super(new Button.ButtonStyle(
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel)),
          new TextureRegionDrawable(Skillicon.getTexture(desc.IconCel + 1))) {{
        disabled = up;
      }});
      this.skill = skill;
      this.desc = desc;
      this.details = new Details();

      this.label = new Label(LARGE_LABEL_STYLE);
      setLayoutEnabled(false); // disables centering of label
      setCullingArea(null); // disabled culling of label, since it's outside button bounds
      add(label); // has to be after setLayoutDisabled, otherwise contents are empty for some reason
    }

    void update(int sLvl) {
      this.sLvl = sLvl;
      details.update();
      label.setStyle(sLvl > 9 ? SMALL_LABEL_STYLE : LARGE_LABEL_STYLE);
      label.setText(sLvl > 0 ? Integer.toString(sLvl) : "");
      label.setPosition(52, -5, Align.center);
      setDisabled(sLvl <= 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      if (isOver()) {
        Riiablo.game.setDetails(details, null, SpellsPanel.this, this);
      }
    }

    private class Details extends Table {
      void update() {
        clearChildren();
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
        if (sLvl <= 0) {
          add(new Label(Riiablo.string.lookup("skilldesc3") + skill.reqlevel, font)).center().space(SPACING).row();
        }
        add().height(font.getLineHeight()).center().space(SPACING).row();
        for (int i = 0; i < desc.dsc2line.length; i++) {
          if (desc.dsc2line[i] <= 0) break;
          String str = calc(desc, i, desc.descline, desc.dsc2texta, desc.dsc2textb, desc.dsc2calca, desc.dsc2calcb, skill, sLvl);
          if (str != null) add(new Label(str, Riiablo.fonts.font16)).center().space(SPACING).row();
        }
        add().height(font.getLineHeight()).center().space(SPACING).row();
        add(new Label(sLvl <= 0 ? Riiablo.string.lookup("StrSkill17") : Riiablo.string.lookup("StrSkill2") + sLvl, font)).center().space(SPACING).row();
        for (int i = 0; i < desc.descline.length; i++) {
          if (desc.descline[i] <= 0) break;
          String str = calc(desc, i, desc.descline, desc.desctexta, desc.desctextb, desc.desccalca, desc.desccalcb, skill, sLvl);
          if (str != null) add(new Label(str, Riiablo.fonts.font16)).center().space(SPACING).row();
        }
        if (sLvl > 0) {
          add().height(font.getLineHeight()).center().space(SPACING).row();
          add(new Label(Riiablo.string.lookup("StrSkill1"), font)).center().space(SPACING).row();
          for (int i = 0; i < desc.descline.length; i++) {
            if (desc.descline[i] <= 0) break;
            String str = calc(desc, i, desc.descline, desc.desctexta, desc.desctextb, desc.desccalca, desc.desccalcb, skill, sLvl + 1);
            if (str != null) add(new Label(str, Riiablo.fonts.font16)).center().space(SPACING).row();
          }
        }
        add().height(font.getLineHeight()).center().space(SPACING).row();
        //add(new Label(Riiablo.string.format("Sksyn", Riiablo.string.lookup(desc.str_name)), font, Riiablo.colors.green)).center().space(SPACING).row();
        for (int i = 0; i < desc.dsc3line.length; i++) {
          if (desc.dsc3line[i] <= 0) break;
          String str = calc(desc, i, desc.dsc3line, desc.dsc3texta, desc.dsc3textb, desc.dsc3calca, desc.dsc3calcb, skill, sLvl);
          if (str != null) add(new Label(str, Riiablo.fonts.font16, desc.dsc3line[i] == 40 ? SpellsPanel.getColor(desc.dsc3calca[i]) : Riiablo.colors.white)).center().space(SPACING).row();
        }
        pack();
      }
    }
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
