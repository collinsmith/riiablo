package com.riiablo.panel;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.riiablo.CharacterClass;
import com.riiablo.Keys;
import com.riiablo.Riiablo;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.SkillDesc;
import com.riiablo.codec.excel.Skills;
import com.riiablo.graphics.BlendMode;
import com.riiablo.item.Stat;
import com.riiablo.key.MappedKey;
import com.riiablo.loader.DC6Loader;
import com.riiablo.screen.GameScreen;
import com.riiablo.widget.Button;
import com.riiablo.widget.HotkeyButton;
import com.riiablo.widget.Label;

import org.apache.commons.lang3.ArrayUtils;

public class ControlPanel extends Table implements Disposable {
  private static final String TAG = "ControlPanel";
  private static final boolean DEBUG_MOBILE = !true;

  final AssetDescriptor<DC6> ctrlpnlDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\ctrlpnl7.DC6", DC6.class);
  HealthWidget healthWidget;
  ManaWidget manaWidget;
  ControlWidget controlWidget;

  final AssetDescriptor<DC6> hlthmanaDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\hlthmana.DC6", DC6.class);
  DC6 hlthmana;

  final AssetDescriptor<DC6> overlapDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\overlap.DC6", DC6.class);
  DC6 overlap;

  final AssetDescriptor<DC6> popbeltDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\ctrlpnl_popbelt.DC6", DC6.class);
  TextureRegion popbelt;

  final AssetDescriptor<DC6> SkilliconDescriptor = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\Skillicon.DC6", DC6.class);
  DC6 Skillicon;

  final AssetDescriptor<DC6> CharSkilliconDescriptor[];
  DC6 CharSkillicon[];

  private static int getClassId(String charClass) {
    if (charClass.isEmpty()) return -1;
    switch (charClass.charAt(0)) {
      case 'a': return charClass.charAt(1) == 'm' ? CharacterClass.AMAZON.id : CharacterClass.ASSASSIN.id;
      case 'b': return CharacterClass.BARBARIAN.id;
      case 'd': return CharacterClass.DRUID.id;
      case 'n': return CharacterClass.NECROMANCER.id;
      case 'p': return CharacterClass.PALADIN.id;
      case 's': return CharacterClass.SORCERESS.id;
      default:  return -1;
    }
  }

  private DC getSkillicon(String charClass, int i) {
    int classId = getClassId(charClass);
    DC icons = classId == -1 ? Skillicon : CharSkillicon[classId];
    return i < icons.getNumPages() ? icons : null;
  }

  HotkeyButton leftSkill, rightSkill;

  GameScreen gameScreen;

  public ControlPanel(final GameScreen gameScreen) {
    this.gameScreen = gameScreen;
    Riiablo.assets.load(hlthmanaDescriptor);
    Riiablo.assets.finishLoadingAsset(hlthmanaDescriptor);
    hlthmana = Riiablo.assets.get(hlthmanaDescriptor);

    Riiablo.assets.load(overlapDescriptor);
    Riiablo.assets.finishLoadingAsset(overlapDescriptor);
    overlap = Riiablo.assets.get(overlapDescriptor);

    Riiablo.assets.load(ctrlpnlDescriptor);
    Riiablo.assets.finishLoadingAsset(ctrlpnlDescriptor);
    DC6 ctrlpnl = Riiablo.assets.get(ctrlpnlDescriptor);

    Riiablo.assets.load(popbeltDescriptor);
    Riiablo.assets.finishLoadingAsset(popbeltDescriptor);
    popbelt = Riiablo.assets.get(popbeltDescriptor).getTexture();

    Riiablo.assets.load(SkilliconDescriptor);
    Riiablo.assets.finishLoadingAsset(SkilliconDescriptor);
    Skillicon = Riiablo.assets.get(SkilliconDescriptor);

    CharSkilliconDescriptor = new AssetDescriptor[7];
    CharSkillicon = new DC6[CharSkilliconDescriptor.length];
    for (int i = 0; i < CharSkilliconDescriptor.length; i++) {
      CharSkilliconDescriptor[i] = new AssetDescriptor<>("data\\global\\ui\\SPELLS\\" + CharacterClass.get(i).spellIcons + ".DC6", DC6.class, DC6Loader.DC6Parameters.COMBINE);
      Riiablo.assets.load(CharSkilliconDescriptor[i]);
      Riiablo.assets.finishLoadingAsset(CharSkilliconDescriptor[i]);
      CharSkillicon[i] = Riiablo.assets.get(CharSkilliconDescriptor[i]);
    }

    final int numFrames = ctrlpnl.getNumFramesPerDir();
    healthWidget = new HealthWidget(ctrlpnl.getTexture(0));
    manaWidget = new ManaWidget(ctrlpnl.getTexture(numFrames - 2));

    if (!DEBUG_MOBILE && Gdx.app.getType() == Application.ApplicationType.Desktop) {
      int leftSkillId = Riiablo.charData.getSkill(Input.Buttons.LEFT);
      if (leftSkillId > 0) {
        final Skills.Entry skill = Riiablo.files.skills.get(leftSkillId);
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        int iconCel = desc.IconCel;
        DC icons = getSkillicon(skill.charclass, iconCel);
        if (icons == null) {
          icons = Skillicon;
          iconCel = 20;
        }

        leftSkill = new HotkeyButton(icons, iconCel, skill.Id);
        if (skill.aura) leftSkill.setBlendMode(BlendMode.DARKEN, Riiablo.colors.darkenGold);
        int index = Riiablo.charData.getHotkey(Input.Buttons.LEFT, leftSkillId);
        if (index != ArrayUtils.INDEX_NOT_FOUND) {
          MappedKey mapping = Keys.Skill[index];
          leftSkill.map(mapping);
        }
      } else {
        leftSkill = new HotkeyButton(Skillicon, 0, -1);
      }
      leftSkill.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          gameScreen.spellsQuickPanelL.setVisible(!gameScreen.spellsQuickPanelL.isVisible());
        }
      });

      int rightSkillId = Riiablo.charData.getSkill(Input.Buttons.RIGHT);
      if (rightSkillId > 0) {
        final Skills.Entry skill = Riiablo.files.skills.get(rightSkillId);
        final SkillDesc.Entry desc = Riiablo.files.skilldesc.get(skill.skilldesc);
        int iconCel = desc.IconCel;
        DC icons = getSkillicon(skill.charclass, iconCel);
        if (icons == null) {
          icons = Skillicon;
          iconCel = 20;
        }

        rightSkill = new HotkeyButton(icons, iconCel, skill.Id);
        if (skill.aura) rightSkill.setBlendMode(BlendMode.DARKEN, Riiablo.colors.darkenGold);
        int index = Riiablo.charData.getHotkey(Input.Buttons.RIGHT, rightSkillId);
        if (index != ArrayUtils.INDEX_NOT_FOUND) {
          MappedKey mapping = Keys.Skill[index];
          rightSkill.map(mapping);
        }
      } else {
        rightSkill = new HotkeyButton(Skillicon, 0, -1);
      }
      rightSkill.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          gameScreen.spellsQuickPanelR.setVisible(!gameScreen.spellsQuickPanelR.isVisible());
        }
      });

      int width = 0;
      int height = Integer.MIN_VALUE;
      for (int i = 1; i < numFrames - 2; i++) {
        Pixmap frame = ctrlpnl.getPixmap(0, i);
        width += frame.getWidth();
        height = Math.max(height, frame.getHeight());
      }
      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      pixmap.setBlending(Pixmap.Blending.None);
      int x = 0;
      for (int i = 1; i < numFrames - 2; i++) {
        Pixmap frame = ctrlpnl.getPixmap(0, i);
        pixmap.drawPixmap(frame, x, pixmap.getHeight() - frame.getHeight());
        x += frame.getWidth();
      }

      controlWidget = new ControlWidget(new Texture(new PixmapTextureData(pixmap, null, false, false, false)));
    }

    final float height = controlWidget == null ? 0 : controlWidget.background.getHeight() - 7;
    add(healthWidget).height(height).growX().left().bottom();
    if (leftSkill != null) add(leftSkill).bottom();
    if (controlWidget != null) add(controlWidget).size(controlWidget.background.getWidth(), height).bottom();
    if (rightSkill != null) add(rightSkill).bottom();
    add(manaWidget).height(height).growX().right().bottom();
    pack();

    //setHeight(controlWidget.background.getHeight() - 7);
    //setY(0);
    setTouchable(Touchable.childrenOnly);
    //setDebug(true, true);
  }

  public HotkeyButton getLeftSkill() {
    return leftSkill;
  }

  public HotkeyButton getRightSkill() {
    return rightSkill;
  }

  @Override
  public void dispose() {
    Riiablo.assets.unload(ctrlpnlDescriptor.fileName);
    Riiablo.assets.unload(popbeltDescriptor.fileName);
    Riiablo.assets.unload(overlapDescriptor.fileName);
    Riiablo.assets.unload(hlthmanaDescriptor.fileName);
    Riiablo.assets.unload(SkilliconDescriptor.fileName);
    if (controlWidget != null) controlWidget.dispose();
  }

  private class HealthWidget extends Actor {
    TextureRegion background;
    TextureRegion health;
    TextureRegion overlay;
    Label label;

    HealthWidget(TextureRegion background) {
      this.background = background;
      //setSize(background.getRegionWidth(), background.getRegionHeight());
      setWidth(background.getRegionWidth());
      health = hlthmana.getTexture(0);
      overlay = overlap.getTexture(0);
      setTouchable(Touchable.enabled);
      label = new Label(Riiablo.fonts.font16);
      label.setY(background.getRegionHeight());
      label.setVisible(!DEBUG_MOBILE && Gdx.app.getType() == Application.ApplicationType.Desktop);
    }

    @Override
    public void draw(Batch batch, float a) {
      final float x = getX();
      final float y = getY();
      batch.draw(background, x, y);
      batch.draw(health,  x + 30, y + 14);
      batch.draw(overlay, x + 28, y +  6);
      super.draw(batch, a);
      if (label.isVisible()) {
        label.setText(Riiablo.string.format("panelhealth",
            (int) Riiablo.charData.getStats().get(Stat.hitpoints).toFloat(),
            (int) Riiablo.charData.getStats().get(Stat.maxhp).toFloat()));
        label.draw(batch, a);
      }
    }
  }
  private class ManaWidget extends Actor {
    TextureRegion background;
    TextureRegion mana;
    TextureRegion overlay;
    Label label;

    ManaWidget(TextureRegion background) {
      this.background = background;
      //setSize(background.getRegionWidth(), background.getRegionHeight());
      setWidth(background.getRegionWidth());
      mana = hlthmana.getTexture(1);
      overlay = overlap.getTexture(1);
      setTouchable(Touchable.enabled);
      label = new Label(Riiablo.fonts.font16);
      label.setY(background.getRegionHeight());
      label.setVisible(!DEBUG_MOBILE && Gdx.app.getType() == Application.ApplicationType.Desktop);
    }

    @Override
    public void draw(Batch batch, float a) {
      final float x = getX();
      final float y = getY();
      batch.draw(background, x, y);
      batch.draw(mana,    x + 8, y + 14);
      batch.draw(overlay, x + 8, y + 10);
      super.draw(batch, a);
      if (label.isVisible()) {
        label.setX(getX() - 32);
        label.setText(Riiablo.string.format("panelmana",
            (int) Riiablo.charData.getStats().get(Stat.mana).toFloat(),
            (int) Riiablo.charData.getStats().get(Stat.maxmana).toFloat()));
        label.draw(batch, a);
      }
    }
  }
  private class ControlWidget extends WidgetGroup implements Disposable {
    final AssetDescriptor<DC6> menubuttonDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\menubutton.DC6", DC6.class);
    Button btnMenu;
    Button.ButtonStyle menuHidden, menuShown;

    final AssetDescriptor<DC6> minipanelDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanel.dc6", DC6.class);
    MinipanelWidget minipanelWidget;

    Texture background;
    ControlWidget(Texture background) {
      this.background = background;
      setSize(background.getWidth(), background.getHeight() - 7);
      setTouchable(Touchable.enabled);

      Riiablo.assets.load(menubuttonDescriptor);
      Riiablo.assets.finishLoadingAsset(menubuttonDescriptor);
      menuHidden = new Button.ButtonStyle() {{
        up   = new TextureRegionDrawable(Riiablo.assets.get(menubuttonDescriptor).getTexture(0));
        down = new TextureRegionDrawable(Riiablo.assets.get(menubuttonDescriptor).getTexture(1));
      }};
      menuShown = new Button.ButtonStyle() {{
        up   = new TextureRegionDrawable(Riiablo.assets.get(menubuttonDescriptor).getTexture(2));
        down = new TextureRegionDrawable(Riiablo.assets.get(menubuttonDescriptor).getTexture(3));
      }};
      btnMenu = new Button(menuHidden);
      btnMenu.setPosition((getWidth() / 2) - (btnMenu.getWidth() / 2), 15);
      btnMenu.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          btnMenu.setStyle(btnMenu.getStyle() == menuHidden ? menuShown : menuHidden);
          minipanelWidget.setVisible(!minipanelWidget.isVisible());
        }
      });
      addActor(btnMenu);

      Riiablo.assets.load(minipanelDescriptor);
      Riiablo.assets.finishLoadingAsset(minipanelDescriptor);
      minipanelWidget = new MinipanelWidget(Riiablo.assets.get(minipanelDescriptor).getTexture(0));
      minipanelWidget.setPosition((getWidth() / 2) - (minipanelWidget.getWidth() / 2), getHeight());
      addActor(minipanelWidget);

      final BeltGrid belt = new BeltGrid(gameScreen, 4, 4, 31, 31);
      belt.setRows(4);
      belt.setBackground(popbelt);
      belt.setPosition(177, 8);
      belt.populate(Riiablo.charData.getBelt());
      addActor(belt);
      //setDebug(true, true);
    }

    @Override
    public void dispose() {
      btnMenu.dispose();
      Riiablo.assets.unload(minipanelDescriptor.fileName);
      minipanelWidget.dispose();
      background.dispose();
      Riiablo.assets.unload(menubuttonDescriptor.fileName);
    }

    @Override
    public void draw(Batch batch, float a) {
      batch.draw(background, getX(), getY());
      super.draw(batch, a);
    }

    private class MinipanelWidget extends WidgetGroup implements Disposable {
      final AssetDescriptor<DC6> minipanelbtnDescriptor = new AssetDescriptor<>("data\\global\\ui\\PANEL\\minipanelbtn.DC6", DC6.class);

      Button btnCharacter;
      Button btnInventory;
      Button btnSkillTree;
      Button btnParty;
      Button btnMap;
      Button btnMessages;
      Button btnQuests;
      Button btnEscapeMenu;

      TextureRegion background;
      MinipanelWidget(TextureRegion background) {
        this.background = background;
        setSize(background.getRegionWidth(), background.getRegionHeight());
        setVisible(false);

        Riiablo.assets.load(minipanelbtnDescriptor);
        Riiablo.assets.finishLoadingAsset(minipanelbtnDescriptor);
        ClickListener clickListener = new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getListenerActor();
            if (actor == btnCharacter) {
              gameScreen.characterPanel.setVisible(!gameScreen.characterPanel.isVisible());
            } else if (actor == btnInventory) {
              gameScreen.inventoryPanel.setVisible(!gameScreen.inventoryPanel.isVisible());
            } else if (actor == btnSkillTree) {
              gameScreen.spellsPanel.setVisible(!gameScreen.spellsPanel.isVisible());
            } else if (actor == btnParty) {

            } else if (actor == btnMap) {

            } else if (actor == btnMessages) {

            } else if (actor == btnQuests) {
              gameScreen.questsPanel.setVisible(!gameScreen.questsPanel.isVisible());
            } else if (actor == btnEscapeMenu) {
              gameScreen.escapePanel.setVisible(!gameScreen.escapePanel.isVisible());
            }
          }
        };
        btnCharacter = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(0));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(1));
        }});
        btnCharacter.addListener(clickListener);
        btnInventory = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(2));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(3));
        }});
        btnInventory.addListener(clickListener);
        btnSkillTree = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(4));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(5));
        }});
        btnSkillTree.addListener(clickListener);
        btnParty = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(6));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(7));
        }});
        btnParty.addListener(clickListener);
        btnMap = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(8));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(9));
        }});
        btnMap.addListener(clickListener);
        btnMessages = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(10));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(11));
        }});
        btnMessages.addListener(clickListener);
        btnQuests = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(12));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(13));
        }});
        btnQuests.addListener(clickListener);
        btnEscapeMenu = new Button(new Button.ButtonStyle() {{
          up   = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(14));
          down = new TextureRegionDrawable(Riiablo.assets.get(minipanelbtnDescriptor).getTexture(15));
        }});
        btnEscapeMenu.addListener(clickListener);
        Table table = new Table();
        table.setFillParent(true);
        table.align(Align.topLeft);
        table.pad(3);
        table.add(btnCharacter).space(1);
        table.add(btnInventory).space(1);
        table.add(btnSkillTree).space(1);
        table.add(btnParty).space(1);
        table.add(btnMap).space(1);
        table.add(btnMessages).space(1);
        table.add(btnQuests).space(1);
        table.add(btnEscapeMenu).space(1);
        addActor(table);
      }

      @Override
      public void dispose() {
        btnCharacter.dispose();
        btnInventory.dispose();
        Riiablo.assets.unload(minipanelbtnDescriptor.fileName);
      }

      @Override
      public void draw(Batch batch, float a) {
        batch.draw(background, getX(), getY());
        super.draw(batch, a);
      }
    }
  }
}
